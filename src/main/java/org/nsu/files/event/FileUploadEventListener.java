package org.nsu.files.event;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class FileUploadEventListener {

    private final StorageService storageService;
    private final MinIOConfigProperties minioProperties;
    private final FileRepository fileRepository;
    private final FileTypeRepository fileTypeRepository;
    private final AnimalCardRepository animalCardRepository;
    private final AnimalCardFileRepository animalCardFileRepository;
    private final AnimalCardFileTypeRepository animalCardFileTypeRepository;

    @Autowired
    public FileUploadEventListener(StorageService storageService, MinIOConfigProperties minioProperties,
                                   FileRepository fileRepository, FileTypeRepository fileTypeRepository,
                                   AnimalCardRepository animalCardRepository, AnimalCardFileRepository animalCardFileRepository,
                                   AnimalCardFileTypeRepository animalCardFileTypeRepository) {
        this.storageService = storageService;
        this.minioProperties = minioProperties;
        this.fileRepository = fileRepository;
        this.fileTypeRepository = fileTypeRepository;
        this.animalCardRepository = animalCardRepository;
        this.animalCardFileRepository = animalCardFileRepository;
        this.animalCardFileTypeRepository = animalCardFileTypeRepository;
    }

    @EventListener
    @Transactional
    public void handleFileUpload(FileUploadEvent event) {
        List<MultipartFile> files = event.getFiles();
        MetadataDTO metadata = event.getMetadata();
        Long userId = event.getUserId();
        Long adId = event.getAdId();

        AnimalCard animalCard = animalCardRepository.findById(adId).orElseThrow(() -> new RuntimeException("AnimalCard not found"));

        List<CompletableFuture<Void>> uploadFutures = new ArrayList<>();
        List<String> uploadedObjectNames = new ArrayList<>();
        List<File> savedFiles = new ArrayList<>();
        List<AnimalCardFile> savedCardFiles = new ArrayList<>();

        try {
            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                FileDescriptor descriptor = metadata.descriptors().get(i);

                if (descriptor.uploadingStatus() != FileDescriptor.UploadingStatus.OK) {
                    continue;
                }

                String extension = getFileExtension(file.getOriginalFilename());
                String typeFolder = descriptor.fileType() == FileDescriptor.FileType.PHOTO ? "photos" : "documents";
                String objectName = String.format("uploads/users/%d/ads/%d/%s/%s.%s", userId, adId, typeFolder, UUID.randomUUID(), extension);

                CompletableFuture<Void> uploadFuture = CompletableFuture.runAsync(() -> {
                    try {
                        storageService.upload(file, minioProperties.bucketName(), objectName);
                    } catch (Exception e) {
                        throw new RuntimeException("Failed to upload file to storage: " + objectName, e);
                    }
                });
                uploadFutures.add(uploadFuture);
                uploadedObjectNames.add(objectName);
            }

            CompletableFuture.allOf(uploadFutures.toArray(new CompletableFuture[0])).join();

            for (int i = 0; i < files.size(); i++) {
                MultipartFile file = files.get(i);
                FileDescriptor descriptor = metadata.descriptors().get(i);

                if (descriptor.uploadingStatus() != FileDescriptor.UploadingStatus.OK) {
                    continue;
                }

                String objectName = uploadedObjectNames.get(i);

                FileType fileType = fileTypeRepository.findByName(descriptor.fileType().name().toLowerCase());
                if (fileType == null) {
                    throw new RuntimeException("FileType not found: " + descriptor.fileType().name().toLowerCase());
                }
                File fileEntity = new File();
                fileEntity.setName(descriptor.originalFilename());
                fileEntity.setLink(objectName);
                fileEntity.setType(fileType);
                File savedFile = fileRepository.save(fileEntity);
                savedFiles.add(savedFile);

                AnimalCardFileType cardFileType = animalCardFileTypeRepository.findByName(descriptor.fileType().name().toLowerCase());
                if (cardFileType == null) {
                    throw new RuntimeException("AnimalCardFileType not found: " + descriptor.fileType().name().toLowerCase());
                }
                AnimalCardFile cardFile = new AnimalCardFile();
                cardFile.setAnimalCard(animalCard);
                cardFile.setFile(savedFile);
                cardFile.setFileType(cardFileType);
                AnimalCardFile savedCardFile = animalCardFileRepository.save(cardFile);
                savedCardFiles.add(savedCardFile);
            }

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

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}
