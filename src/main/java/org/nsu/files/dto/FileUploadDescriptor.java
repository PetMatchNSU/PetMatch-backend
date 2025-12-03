package org.nsu.files.dto;

public record FileUploadDescriptor(
    String originalFilename,
    String uploadingStatus,
    String fileId,
    String cardId
) {
}
