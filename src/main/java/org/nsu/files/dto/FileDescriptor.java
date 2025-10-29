package org.nsu.files.dto;

import lombok.Builder;

@Builder
public record FileDescriptor(
    String originalFilename,
    boolean isMain,
    FileType fileType,
    UploadingStatus uploadingStatus,
    DeletingStatus deletingStatus,
    Long fileId,
    Long cardId,
    String content
) {
    public enum FileType {
        PHOTO, DOC
    }

    public enum UploadingStatus {
        OK, NOT_VALID, INTERNAL_ERROR
    }

    public enum DeletingStatus {
        DELETED, INTERNAL_ERROR
    }
}
