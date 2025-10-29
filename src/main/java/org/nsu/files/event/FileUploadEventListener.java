package org.nsu.files.event;

import lombok.RequiredArgsConstructor;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.AnimalCardFile;
import org.nsu.animal.entity.AnimalCardFileType;
import org.nsu.animal.repository.AnimalCardFileRepository;
import org.nsu.animal.repository.AnimalCardFileTypeRepository;
import org.nsu.animal.repository.AnimalCardRepository;
import org.nsu.files.config.MinIOConfigProperties;
import org.nsu.files.dto.FileDescriptor;
import org.nsu.files.dto.MetadataDTO;
import org.nsu.files.entity.File;
import org.nsu.files.entity.FileType;
import org.nsu.files.repository.FileRepository;
import org.nsu.files.repository.FileTypeRepository;
import org.nsu.files.storage.StorageService;
import org.nsu.files.util.FileUtils;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
@RequiredArgsConstructor
public class FileUploadEventListener {

    private static final String UPLOAD_PATH_TEMPLATE = "uploads/users/%d/ads/%d/%s/%s.%s";

    private final StorageService storageService;
    private final MinIOConfigProperties minioProperties;
    private final FileRepository fileRepository;
    private final FileTypeRepository fileTypeRepository;
    private final AnimalCardRepository animalCardRepository;
    private final AnimalCardFileRepository animalCardFileRepository;
    private final AnimalCardFileTypeRepository animalCardFileTypeRepository;

    @EventListener
    public void handleFileUpload(FileUploadEvent event) {
        List<MultipartFile> files = event.getFiles();
        MetadataDTO metadata = event.getMetadata();
        Long userId = event.getUserId();
        Long adId = event.getAdId();

        List<CompletableFuture<Void>> uploadFutures = new ArrayList<>();
        List<String> uploadedObjectNames = new ArrayList<>();
        List<FileMetadata> fileMetadataList = new ArrayList<>();

        try {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                FileDescriptor descriptor = metadata.descriptors().get(i);

                if (descriptor.uploadingStatus() != FileDescriptor.UploadingStatus.OK) {
                    continue;
                }

                String extension = FileUtils.getFileExtension(file.getOriginalFilename());
                String typeFolder = descriptor.fileType() == FileDescriptor.FileType.PHOTO ? "photos" : "documents";
                String objectName = String.format(UPLOAD_PATH_TEMPLATE, userId, adId, typeFolder, UUID.randomUUID(), extension);

                CompletableFuture<Void> uploadFuture = CompletableFuture.runAsync(() -> {
                    try {
                        storageService.upload(file, minioProperties.bucketName(), objectName);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to upload file to storage: " + objectName, e);
                    }
                });
                uploadFutures.add(uploadFuture);
                uploadedObjectNames.add(objectName);
                fileMetadataList.add(new FileMetadata(descriptor, objectName));
            }

            CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();

            // After successful upload, save metadata in a separate transactional method
            saveFileMetadata(adId, fileMetadataList);

        } catch (Exception e) {
            for (String objectName : uploadedObjectNames) {
                try {
                    storageService.delete(objectName, minioProperties.bucketName());
                } catch (Exception deleteEx) {
                    System.err.println("Failed to delete file from storage: " + objectName + " - " + deleteEx.getMessage());
                }
            }
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }

    @Transactional
    private void saveFileMetadata(Long adId, List<FileMetadata> fileMetadataList) {
        AnimalCard animalCard = animalCardRepository.findById(adId).orElseThrow(() -> new RuntimeException("AnimalCard not found"));

        for (FileMetadata fileMetadata : fileMetadataList) {
            FileDescriptor descriptor = fileMetadata.descriptor;
            String objectName = fileMetadata.objectName;

            FileType fileType = fileTypeRepository.findByName(descriptor.fileType().name().toLowerCase());
            if (fileType == null) {
                throw new RuntimeException("FileType not found: " + descriptor.fileType().name().toLowerCase());
            }
            File fileEntity = new File();
            fileEntity.setName(descriptor.originalFilename());
            fileEntity.setLink(objectName);
            fileEntity.setType(fileType);
            File savedFile = fileRepository.save(fileEntity);

            AnimalCardFileType cardFileType = animalCardFileTypeRepository.findByName(descriptor.fileType().name().toLowerCase());
            if (cardFileType == null) {
                throw new RuntimeException("AnimalCardFileType not found: " + descriptor.fileType().name().toLowerCase());
            }
            AnimalCardFile cardFile = new AnimalCardFile();
            cardFile.setAnimalCard(animalCard);
            cardFile.setFile(savedFile);
            cardFile.setFileType(cardFileType);
            animalCardFileRepository.save(cardFile);
        }
    }

    private static class FileMetadata {
        final FileDescriptor descriptor;
        final String objectName;

        FileMetadata(FileDescriptor descriptor, String objectName) {
            this.descriptor = descriptor;
            this.objectName = objectName;
        }
    }


}
