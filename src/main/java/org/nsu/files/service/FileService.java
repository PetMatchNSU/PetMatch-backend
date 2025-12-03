package org.nsu.files.service;

import org.nsu.files.config.FileValidationProperties;
import org.nsu.files.dto.FileDescriptor;
import org.nsu.files.dto.MetadataDTO;
import org.nsu.files.dto.FilterDTO;
import org.nsu.files.dto.DeleteRequest;
import org.nsu.files.dto.FileUploadResponse;
import org.nsu.files.dto.FileUploadDescriptor;
import org.nsu.files.event.FileUploadEvent;
import org.nsu.files.repository.FileRepository;
import org.nsu.files.repository.FileTypeRepository;
import org.nsu.animal.repository.AnimalCardFileRepository;
import org.nsu.animal.repository.AnimalCardRepository;
import org.nsu.animal.repository.AnimalCardFileTypeRepository;
import org.nsu.files.storage.StorageService;
import org.nsu.files.util.FileUtils;
import org.nsu.files.entity.File;
import org.nsu.files.entity.FileType;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.AnimalCardFile;
import org.nsu.animal.entity.AnimalCardFileType;
import org.nsu.files.config.MinIOConfigProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.nsu.authorization.core.security.PersonDetails;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Base64;
import java.util.stream.Collectors;
import java.io.InputStream;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.nio.charset.StandardCharsets;
import org.nsu.animal.entity.AnimalCardFile;

@RequiredArgsConstructor
@Service
public class FileService {

    private final FileValidationProperties validationProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final FileRepository fileRepository;
    private final FileTypeRepository fileTypeRepository;
    private final AnimalCardFileRepository animalCardFileRepository;
    private final AnimalCardRepository animalCardRepository;
    private final AnimalCardFileTypeRepository animalCardFileTypeRepository;
    private final StorageService storageService;
    private final MinIOConfigProperties minioProperties;
    private final ObjectMapper objectMapper;

    public FileUploadResponse uploadFiles(MultipartFile[] files, String metadataJson, Long adId) throws JsonProcessingException {
        MetadataDTO metadata = objectMapper.readValue(metadataJson, MetadataDTO.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        Long userId = personDetails.getUserId();
        if (files.length != metadata.descriptors().size()) {
            throw new IllegalArgumentException("Number of files does not match number of descriptors");
        }
        List<FileUploadDescriptor> descriptors = validateFiles(List.of(files), metadata.descriptors(), adId);
        eventPublisher.publishEvent(new FileUploadEvent(this, List.of(files), metadata, userId, adId));
        return new FileUploadResponse(descriptors);
    }

    private List<FileUploadDescriptor> validateFiles(List<MultipartFile> files, List<FileDescriptor> inputDescriptors, Long adId) {
        List<FileUploadDescriptor> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            FileDescriptor descriptor = inputDescriptors.get(i);
            String status;

            if (file.getSize() > validationProperties.maxSizeBytes()) {
                status = "not_valid";
            } else {
                try {
                    String mimeType = FileUtils.detectMimeType(file.getBytes());
                    String extension = FileUtils.getFileExtensionFromMimeType(mimeType);
                    List<String> allowedFormats = descriptor.fileType() == FileDescriptor.FileType.PHOTO ? validationProperties.photoFormats() : validationProperties.docFormats();
                    if (!allowedFormats.contains(extension.toUpperCase())) {
                        status = "not_valid";
                    } else {
                        status = "ok";
                    }
                } catch (Exception e) {
                    status = "internal_error";
                }
            }

            String filename = descriptor.originalFilename() != null ? descriptor.originalFilename() : file.getOriginalFilename();
            result.add(new FileUploadDescriptor(filename, status, null, String.valueOf(adId)));
        }
        return result;
    }

    public MetadataDTO getFiles(String query) {
        try {
            String decodedQuery = new String(Base64.getDecoder().decode(query), StandardCharsets.UTF_8);
            FilterDTO filter = objectMapper.readValue(decodedQuery, FilterDTO.class);
            return getFiles(filter);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse query", e);
        }
    }

    private MetadataDTO getFiles(FilterDTO filter) {
        Set<AnimalCardFile> animalCardFilesSet = new HashSet<>();

        if (filter.fileIds() != null && !filter.fileIds().isEmpty()) {
            animalCardFilesSet.addAll(animalCardFileRepository.findByFileIdIn(filter.fileIds()));
        }
        if (filter.cardIds() != null && !filter.cardIds().isEmpty()) {
            animalCardFilesSet.addAll(animalCardFileRepository.findByAnimalCardIdIn(filter.cardIds()));
        }
        if (animalCardFilesSet.isEmpty()) {
            animalCardFilesSet.addAll(animalCardFileRepository.findAll());
        }

        List<AnimalCardFile> animalCardFiles = new ArrayList<>(animalCardFilesSet);

        if (filter.isMain() != null) {
            animalCardFiles = animalCardFiles.stream()
                .filter(acf -> (acf.getFileType().getName().equals("photo")) == filter.isMain())
                .collect(Collectors.toList());
        }
        if (filter.fileTypes() != null && !filter.fileTypes().isEmpty()) {
            animalCardFiles = animalCardFiles.stream()
                .filter(acf -> filter.fileTypes().contains(acf.getFileType().getName().toLowerCase()))
                .collect(Collectors.toList());
        }

        List<FileDescriptor> descriptors = animalCardFiles.stream().map(acf -> {
            try {
                InputStream inputStream = storageService.get(null, acf.getFile().getLink());
                String content = Base64.getEncoder().encodeToString(inputStream.readAllBytes());
                return FileDescriptor.builder()
                    .originalFilename(acf.getFile().getName())
                    .isMain(acf.getFileType().getName().equals("photo"))
                    .fileType(FileDescriptor.FileType.valueOf(acf.getFileType().getName().toUpperCase()))
                    .fileId(acf.getFile().getId())
                    .cardId(acf.getAnimalCard().getId())
                    .content(content)
                    .build();
            } catch (Exception e) {
                return FileDescriptor.builder()
                    .originalFilename(acf.getFile().getName())
                    .isMain(acf.getFileType().getName().equals("photo"))
                    .fileType(FileDescriptor.FileType.valueOf(acf.getFileType().getName().toUpperCase()))
                    .fileId(acf.getFile().getId())
                    .cardId(acf.getAnimalCard().getId())
                    .build();
            }
        }).collect(Collectors.toList());

        return new MetadataDTO(descriptors);
    }

    public MetadataDTO deleteFiles(DeleteRequest deleteRequest) {
        List<Long> fileIds = deleteRequest.fileIds() != null ? deleteRequest.fileIds() : List.of();
        List<Long> cardIds = deleteRequest.cardIds() != null ? deleteRequest.cardIds() : List.of();

        List<AnimalCardFile> animalCardFiles = animalCardFileRepository.findByFileIdIn(fileIds);
        for (Long cardId : cardIds) {
            animalCardFiles.addAll(animalCardFileRepository.findByAnimalCardId(cardId));
        }

        List<FileDescriptor> descriptors = animalCardFiles.stream().map(acf -> {
            try {
                storageService.delete(null, acf.getFile().getLink());
                animalCardFileRepository.delete(acf);
                fileRepository.delete(acf.getFile());
                return FileDescriptor.builder()
                    .originalFilename(acf.getFile().getName())
                    .isMain(acf.getFileType().getName().equals("photo"))
                    .fileType(FileDescriptor.FileType.valueOf(acf.getFileType().getName().toUpperCase()))
                    .deletingStatus(FileDescriptor.DeletingStatus.DELETED)
                    .fileId(acf.getFile().getId())
                    .cardId(acf.getAnimalCard().getId())
                    .build();
            } catch (Exception e) {
                return FileDescriptor.builder()
                    .originalFilename(acf.getFile().getName())
                    .isMain(acf.getFileType().getName().equals("photo"))
                    .fileType(FileDescriptor.FileType.valueOf(acf.getFileType().getName().toUpperCase()))
                    .deletingStatus(FileDescriptor.DeletingStatus.INTERNAL_ERROR)
                    .fileId(acf.getFile().getId())
                    .cardId(acf.getAnimalCard().getId())
                    .build();
            }
        }).collect(Collectors.toList());

        return new MetadataDTO(descriptors);
    }

}
