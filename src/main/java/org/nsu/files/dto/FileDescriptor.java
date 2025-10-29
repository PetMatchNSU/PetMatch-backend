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
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String originalFilename;
        private boolean isMain;
        private FileType fileType;
        private UploadingStatus uploadingStatus;
        private DeletingStatus deletingStatus;
        private Long fileId;
        private Long cardId;
        private String content;

        public Builder originalFilename(String originalFilename) {
            this.originalFilename = originalFilename;
            return this;
        }

        public Builder isMain(boolean isMain) {
            this.isMain = isMain;
            return this;
        }

        public Builder fileType(FileType fileType) {
            this.fileType = fileType;
            return this;
        }

        public Builder uploadingStatus(UploadingStatus uploadingStatus) {
            this.uploadingStatus = uploadingStatus;
            return this;
        }

        public Builder deletingStatus(DeletingStatus deletingStatus) {
            this.deletingStatus = deletingStatus;
            return this;
        }

        public Builder fileId(Long fileId) {
            this.fileId = fileId;
            return this;
        }

        public Builder cardId(Long cardId) {
            this.cardId = cardId;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public FileDescriptor build() {
            return new FileDescriptor(originalFilename, isMain, fileType, uploadingStatus, deletingStatus, fileId, cardId, content);
        }
    }

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
