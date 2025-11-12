package org.nsu.files.service;

import org.nsu.files.config.FileValidationProperties;
import org.nsu.files.dto.FileDescriptor;
import org.nsu.files.dto.MetadataDTO;
import org.nsu.files.dto.FilterDTO;
import org.nsu.files.dto.DeleteRequest;
import org.nsu.files.event.FileUploadEvent;
import org.nsu.files.repository.FileRepository;
import org.nsu.animal.repository.AnimalCardFileRepository;
import org.nsu.files.storage.StorageService;
import org.nsu.files.util.FileUtils;
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
    private final AnimalCardFileRepository animalCardFileRepository;
    private final StorageService storageService;
    private final ObjectMapper objectMapper;

    public MetadataDTO uploadFiles(MultipartFile[] files, String metadataJson, Long adId) throws JsonProcessingException {
        MetadataDTO metadata = objectMapper.readValue(metadataJson, MetadataDTO.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        Long userId = personDetails.getUserId();
        if (files.length != metadata.descriptors().size()) {
            throw new IllegalArgumentException("Number of files does not match number of descriptors");
        }
        List<FileDescriptor> descriptors = validateFiles(List.of(files), metadata.descriptors());
        eventPublisher.publishEvent(new FileUploadEvent(this, List.of(files), metadata, userId, adId));
        return new MetadataDTO(descriptors);
    }

    private List<FileDescriptor> validateFiles(List<MultipartFile> files, List<FileDescriptor> descriptors) {
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            FileDescriptor descriptor = descriptors.get(i);

            if (file.getSize() > validationProperties.maxSizeBytes()) {
                descriptors.set(i, new FileDescriptor(descriptor.originalFilename(), descriptor.isMain(), descriptor.fileType(), FileDescriptor.UploadingStatus.NOT_VALID, null, null, null, null));
                continue;
            }

            try {
                String mimeType = FileUtils.detectMimeType(file.getBytes());
                String extension = FileUtils.getFileExtensionFromMimeType(mimeType);
                List<String> allowedFormats = descriptor.fileType() == FileDescriptor.FileType.PHOTO ? validationProperties.photoFormats() : validationProperties.docFormats();
                if (!allowedFormats.contains(extension.toUpperCase())) {
                    descriptors.set(i, new FileDescriptor(descriptor.originalFilename(), descriptor.isMain(), descriptor.fileType(), FileDescriptor.UploadingStatus.NOT_VALID, null, null, null, null));
                    continue;
                }
            } catch (Exception e) {
                descriptors.set(i, new FileDescriptor(descriptor.originalFilename(), descriptor.isMain(), descriptor.fileType(), FileDescriptor.UploadingStatus.NOT_VALID, null, null, null, null));
                continue;
            }

            descriptors.set(i, new FileDescriptor(descriptor.originalFilename(), descriptor.isMain(), descriptor.fileType(), FileDescriptor.UploadingStatus.OK, null, null, null, null));
        }
        return descriptors;
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
