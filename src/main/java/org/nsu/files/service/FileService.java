package org.nsu.files.service;

import org.nsu.files.config.FileValidationProperties;
import org.nsu.files.dto.FileDescriptor;
import org.nsu.files.dto.MetadataDTO;
import org.nsu.files.dto.FilterDTO;
import org.nsu.files.dto.DeleteRequest;
import org.nsu.files.dto.DeleteResponse;
import org.nsu.files.dto.DeleteDescriptor;
import org.nsu.files.dto.GetResponse;
import org.nsu.files.dto.GetFileDescriptor;
import org.nsu.files.dto.FileUploadResponse;
import org.nsu.files.dto.FileUploadDescriptor;
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
import lombok.extern.slf4j.Slf4j;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@RequiredArgsConstructor
@Service
@Slf4j
public class FileService {

    private static final String UPLOAD_PATH_TEMPLATE = "uploads/users/%d/ads/%d/%s/%s.%s";

    private final FileValidationProperties validationProperties;
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
        uploadAndSaveFiles(files, metadata.descriptors(), descriptors, userId, adId);
        return new FileUploadResponse(descriptors);
    }

    private List<FileUploadDescriptor> validateFiles(List<MultipartFile> files, List<FileDescriptor> inputDescriptors, Long adId) {
        List<FileUploadDescriptor> result = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            FileDescriptor descriptor = inputDescriptors.get(i);
            FileDescriptor.UploadingStatus status;

            if (file.getSize() > validationProperties.maxSizeBytes()) {
                status = FileDescriptor.UploadingStatus.NOT_VALID;
            } else {
                try {
                    String mimeType = FileUtils.detectMimeType(file.getBytes());
                    String detectedExtension = FileUtils.getFileExtensionFromMimeType(mimeType);
                    List<String> allowedFormats = descriptor.fileType() == FileDescriptor.FileType.PHOTO ? validationProperties.photoFormats() : validationProperties.docFormats();
                    if (!allowedFormats.contains(detectedExtension.toUpperCase())) {
                        status = FileDescriptor.UploadingStatus.NOT_VALID;
                    } else {
                        // Check if descriptor filename extension matches detected extension
                        String descriptorExtension = FileUtils.getFileExtension(descriptor.originalFilename());
                        if (!descriptorExtension.isEmpty() && !detectedExtension.equalsIgnoreCase(descriptorExtension)) {
                            status = FileDescriptor.UploadingStatus.NOT_VALID;
                        } else {
                            status = FileDescriptor.UploadingStatus.OK;
                        }
                    }
                } catch (Exception e) {
                    status = FileDescriptor.UploadingStatus.INTERNAL_ERROR;
                }
            }

            String filename = descriptor.originalFilename() != null ? descriptor.originalFilename() : file.getOriginalFilename();
            result.add(new FileUploadDescriptor(filename, status, null, String.valueOf(adId)));
        }
        return result;
    }

    private void uploadAndSaveFiles(MultipartFile[] files, List<FileDescriptor> inputDescriptors, List<FileUploadDescriptor> descriptors, Long userId, Long adId) {
        List<CompletableFuture<Void>> uploadFutures = new ArrayList<>();
        List<String> uploadedObjectNames = new ArrayList<>();
        List<FileMetadata> fileMetadataList = new ArrayList<>();

        try {
            for (int i = 0; i < files.length; i++) {
                MultipartFile file = files[i];
                FileDescriptor descriptor = inputDescriptors.get(i);
                FileUploadDescriptor uploadDescriptor = descriptors.get(i);

                if (uploadDescriptor.uploadingStatus() != FileDescriptor.UploadingStatus.OK) {
                    continue;
                }

                String extension = FileUtils.getFileExtension(file.getOriginalFilename());
                String typeFolder = descriptor.fileType() == FileDescriptor.FileType.PHOTO ? "photos" : "documents";
                String uuid = UUID.randomUUID().toString();
                String objectName = String.format(UPLOAD_PATH_TEMPLATE, userId, adId, typeFolder, uuid, extension);

                CompletableFuture<Void> uploadFuture = CompletableFuture.runAsync(() -> {
                    try {
                        storageService.upload(file, minioProperties.bucketName(), objectName);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to upload file to storage: " + objectName, e);
                    }
                });
                uploadFutures.add(uploadFuture);
                uploadedObjectNames.add(objectName);
                fileMetadataList.add(new FileMetadata(descriptor, objectName, uuid, extension, typeFolder, userId, adId, i));
            }

            CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();

            // After successful upload, save metadata in a separate transactional method
            List<Long> fileIds = saveFileMetadata(adId, fileMetadataList);

            // Update descriptors with fileIds
            for (int j = 0; j < fileMetadataList.size(); j++) {
                FileMetadata fm = fileMetadataList.get(j);
                descriptors.set(fm.index, new FileUploadDescriptor(descriptors.get(fm.index).originalFilename(), descriptors.get(fm.index).uploadingStatus(), String.valueOf(fileIds.get(j)), descriptors.get(fm.index).cardId()));
            }

        } catch (Exception e) {
            for (String objectName : uploadedObjectNames) {
                try {
                    storageService.delete(minioProperties.bucketName(), objectName);
                } catch (Exception deleteEx) {
                    // Ignore
                }
            }
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    private List<Long> saveFileMetadata(Long adId, List<FileMetadata> fileMetadataList) {
        AnimalCard animalCard = animalCardRepository.findById(adId).orElseThrow(() -> new RuntimeException("AnimalCard not found"));
        List<Long> fileIds = new ArrayList<>();

        for (FileMetadata fileMetadata : fileMetadataList) {
            FileDescriptor descriptor = fileMetadata.descriptor;
            String uuid = fileMetadata.uuid;

            FileType fileType = fileTypeRepository.findByName(descriptor.fileType().name().toLowerCase());
            if (fileType == null) {
                throw new RuntimeException("FileType not found: " + descriptor.fileType().name().toLowerCase());
            }
            File fileEntity = new File();
            fileEntity.setName(descriptor.originalFilename());
            fileEntity.setLink(uuid);
            fileEntity.setType(fileType);
            File savedFile = fileRepository.save(fileEntity);
            fileIds.add(savedFile.getId());

            AnimalCardFileType cardFileType = animalCardFileTypeRepository.findByName(descriptor.fileType().name().toLowerCase());
            AnimalCardFile cardFile = new AnimalCardFile();
            cardFile.setAnimalCard(animalCard);
            cardFile.setFile(savedFile);
            cardFile.setFileType(cardFileType); // null
            animalCardFileRepository.save(cardFile);

            if (descriptor.isMain() && descriptor.fileType() == FileDescriptor.FileType.PHOTO) {
                animalCard.setMainPhotoId(savedFile.getId());
                animalCardRepository.save(animalCard);
            }
        }
        return fileIds;
    }

    private static class FileMetadata {
        final FileDescriptor descriptor;
        final String objectName;
        final String uuid;
        final String extension;
        final String typeFolder;
        final Long userId;
        final Long adId;
        final int index;

        FileMetadata(FileDescriptor descriptor, String objectName, String uuid, String extension, String typeFolder, Long userId, Long adId, int index) {
            this.descriptor = descriptor;
            this.objectName = objectName;
            this.uuid = uuid;
            this.extension = extension;
            this.typeFolder = typeFolder;
            this.userId = userId;
            this.adId = adId;
            this.index = index;
        }
    }

    public GetResponse getFiles(String query) {
        try {
            String decodedQuery = new String(Base64.getDecoder().decode(query), StandardCharsets.UTF_8);
            FilterDTO filter = objectMapper.readValue(decodedQuery, FilterDTO.class);
            return getFiles(filter);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse query", e);
        }
    }

    private String constructObjectName(AnimalCardFile acf) {
        Long userId = acf.getAnimalCard().getCardAuthor().getId();
        Long adId = acf.getAnimalCard().getId();
        String typeFolder = acf.getFile().getType().getName().equals("photo") ? "photos" : "documents";
        String extension = FileUtils.getFileExtension(acf.getFile().getName());
        String uuid = acf.getFile().getLink();
        return String.format(UPLOAD_PATH_TEMPLATE, userId, adId, typeFolder, uuid, extension);
    }

    private GetResponse getFiles(FilterDTO filter) {
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
                .filter(acf -> (acf.getFile().getType().getName().equals("photo")) == filter.isMain())
                .collect(Collectors.toList());
        }
        if (filter.fileTypes() != null && !filter.fileTypes().isEmpty()) {
            animalCardFiles = animalCardFiles.stream()
                .filter(acf -> filter.fileTypes().contains(acf.getFile().getType().getName().toLowerCase()))
                .collect(Collectors.toList());
        }

        List<GetFileDescriptor> descriptors = animalCardFiles.stream().map(acf -> {
            String fileTypeName = acf.getFile().getType().getName().toLowerCase();
            boolean isPhoto = acf.getFile().getType().getName().equals("photo");
            try {
                String objectName = constructObjectName(acf);
                log.info("file {}", objectName);
                InputStream inputStream = storageService.get(minioProperties.bucketName(), objectName);
                String content = Base64.getEncoder().encodeToString(inputStream.readAllBytes());
                return new GetFileDescriptor(
                    acf.getFile().getId().toString(),
                    fileTypeName,
                    isPhoto,
                    acf.getFile().getName(),
                    acf.getAnimalCard().getId().toString(),
                    content
                );
            } catch (Exception e) {
                log.warn("Failed to retrieve content for file {} from storage: {}", acf.getFile().getId(), e.getMessage());
                return new GetFileDescriptor(
                    acf.getFile().getId().toString(),
                    fileTypeName,
                    isPhoto,
                    acf.getFile().getName(),
                    acf.getAnimalCard().getId().toString(),
                    null
                );
            }
        }).collect(Collectors.toList());

        return new GetResponse(descriptors);
    }

    public DeleteResponse deleteFiles(DeleteRequest deleteRequest) {
        List<Long> fileIds = deleteRequest.fileIds() != null ? deleteRequest.fileIds() : List.of();
        List<Long> cardIds = deleteRequest.cardIds() != null ? deleteRequest.cardIds() : List.of();

        Set<AnimalCardFile> animalCardFiles = new HashSet<>();
        animalCardFiles.addAll(animalCardFileRepository.findByFileIdIn(fileIds));
        for (Long cardId : cardIds) {
            animalCardFiles.addAll(animalCardFileRepository.findByAnimalCardId(cardId));
        }

        List<DeleteDescriptor> descriptors = animalCardFiles.stream().map(acf -> {
            try {
                String objectName = constructObjectName(acf);
                storageService.delete(null, objectName);
                animalCardFileRepository.delete(acf);
                // Check if this file is the main photo
                if (acf.getAnimalCard().getMainPhotoId() != null && acf.getAnimalCard().getMainPhotoId().equals(acf.getFile().getId())) {
                    acf.getAnimalCard().setMainPhotoId(null);
                    animalCardRepository.save(acf.getAnimalCard());
                }
                fileRepository.delete(acf.getFile());
                return new DeleteDescriptor(acf.getFile().getId(), "deleted");
            } catch (Exception e) {
                return new DeleteDescriptor(acf.getFile().getId(), "internal_error");
            }
        }).collect(Collectors.toList());

        return new DeleteResponse(descriptors);
    }

}
