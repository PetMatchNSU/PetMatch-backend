package org.nsu.files.dto;

import org.nsu.files.dto.FileDescriptor.UploadingStatus;

public record FileUploadDescriptor(
    String originalFilename,
    UploadingStatus uploadingStatus,
    String fileId,
    String cardId
) {
}
