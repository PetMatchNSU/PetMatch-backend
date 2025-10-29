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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Base64;
import java.util.stream.Collectors;
import java.io.InputStream;

@Service
public class FileService {

    private final FileValidationProperties validationProperties;
    private final ApplicationEventPublisher eventPublisher;
    private final FileRepository fileRepository;
    private final AnimalCardFileRepository animalCardFileRepository;
    private final StorageService storageService;

    @Autowired
    public FileService(FileValidationProperties validationProperties, ApplicationEventPublisher eventPublisher,
                       FileRepository fileRepository, AnimalCardFileRepository animalCardFileRepository,
                       StorageService storageService) {
        this.validationProperties = validationProperties;
        this.eventPublisher = eventPublisher;
        this.fileRepository = fileRepository;
        this.animalCardFileRepository = animalCardFileRepository;
        this.storageService = storageService;
    }

    public List<FileDescriptor> validateAndPublishUpload(List<MultipartFile> files, MetadataDTO metadata, Long userId, Long adId) {
        List<FileDescriptor> descriptors = validateFiles(files, metadata.descriptors());

        eventPublisher.publishEvent(new FileUploadEvent(this, files, metadata, userId, adId));

        return descriptors;
    }

    private List<FileDescriptor> validateFiles(List<MultipartFile> files, List<FileDescriptor> descriptors) {
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            FileDescriptor descriptor = descriptors.get(i);

            if (file.getSize() > validationProperties.maxSizeMb() * 1024 * 1024) {
                descriptors.set(i, new FileDescriptor(descriptor.originalFilename(), descriptor.isMain(), descriptor.fileType(), FileDescriptor.UploadingStatus.NOT_VALID, null, null, null, null));
                continue;
            }

            String extension = getFileExtension(file.getOriginalFilename());
            List<String> allowedFormats = descriptor.fileType() == FileDescriptor.FileType.PHOTO ? validationProperties.photoFormats() : validationProperties.docFormats();
            if (!allowedFormats.contains(extension.toUpperCase())) {
                descriptors.set(i, new FileDescriptor(descriptor.originalFilename(), descriptor.isMain(), descriptor.fileType(), FileDescriptor.UploadingStatus.NOT_VALID, null, null, null, null));
                continue;
            }

            descriptors.set(i, new FileDescriptor(descriptor.originalFilename(), descriptor.isMain(), descriptor.fileType(), FileDescriptor.UploadingStatus.OK, null, null, null, null));
        }
        return descriptors;
    }

    public MetadataDTO getFiles(FilterDTO filter) {
        java.util.Set<org.nsu.animal.entity.AnimalCardFile> animalCardFilesSet = new java.util.HashSet<>();

        if (filter.fileIds() != null && !filter.fileIds().isEmpty()) {
            animalCardFilesSet.addAll(animalCardFileRepository.findByFileIdIn(filter.fileIds()));
        }
        if (filter.cardIds() != null && !filter.cardIds().isEmpty()) {
            animalCardFilesSet.addAll(animalCardFileRepository.findByAnimalCardIdIn(filter.cardIds()));
        }
        if (animalCardFilesSet.isEmpty()) {
            animalCardFilesSet.addAll(animalCardFileRepository.findAll());
        }

        List<org.nsu.animal.entity.AnimalCardFile> animalCardFiles = new java.util.ArrayList<>(animalCardFilesSet);

        if (filter.isMain() != null && !"all".equals(filter.isMain())) {
            boolean isMain = "true".equals(filter.isMain());
            animalCardFiles = animalCardFiles.stream()
                .filter(acf -> (acf.getFileType().getName().equals("photo")) == isMain)
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
                return new FileDescriptor(
                    acf.getFile().getName(),
                    acf.getFileType().getName().equals("photo"),
                    FileDescriptor.FileType.valueOf(acf.getFileType().getName().toUpperCase()),
                    null,
                    null,
                    acf.getFile().getId(),
                    acf.getAnimalCard().getId(),
                    content
                );
            } catch (Exception e) {
                return new FileDescriptor(
                    acf.getFile().getName(),
                    acf.getFileType().getName().equals("photo"),
                    FileDescriptor.FileType.valueOf(acf.getFileType().getName().toUpperCase()),
                    null,
                    null,
                    acf.getFile().getId(),
                    acf.getAnimalCard().getId(),
                    null
                );
            }
        }).collect(Collectors.toList());

        return new MetadataDTO(descriptors);
    }

    public MetadataDTO deleteFiles(DeleteRequest deleteRequest) {
        List<Long> fileIds = deleteRequest.fileIds() != null ? deleteRequest.fileIds() : List.of();
        List<Long> cardIds = deleteRequest.cardIds() != null ? deleteRequest.cardIds() : List.of();

        List<org.nsu.animal.entity.AnimalCardFile> animalCardFiles = animalCardFileRepository.findByFileIdIn(fileIds);
        for (Long cardId : cardIds) {
            animalCardFiles.addAll(animalCardFileRepository.findByAnimalCardId(cardId));
        }

        List<FileDescriptor> descriptors = animalCardFiles.stream().map(acf -> {
            try {
                storageService.delete(null, acf.getFile().getLink());
                animalCardFileRepository.delete(acf);
                fileRepository.delete(acf.getFile());
                return new FileDescriptor(
                    acf.getFile().getName(),
                    acf.getFileType().getName().equals("photo"),
                    FileDescriptor.FileType.valueOf(acf.getFileType().getName().toUpperCase()),
                    null,
                    FileDescriptor.DeletingStatus.DELETED,
                    acf.getFile().getId(),
                    acf.getAnimalCard().getId(),
                    null
                );
            } catch (Exception e) {
                return new FileDescriptor(
                    acf.getFile().getName(),
                    acf.getFileType().getName().equals("photo"),
                    FileDescriptor.FileType.valueOf(acf.getFileType().getName().toUpperCase()),
                    null,
                    FileDescriptor.DeletingStatus.INTERNAL_ERROR,
                    acf.getFile().getId(),
                    acf.getAnimalCard().getId(),
                    null
                );
            }
        }).collect(Collectors.toList());

        return new MetadataDTO(descriptors);
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
