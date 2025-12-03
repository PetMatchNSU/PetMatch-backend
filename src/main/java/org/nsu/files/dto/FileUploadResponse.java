package org.nsu.files.dto;

import java.util.List;

public record FileUploadResponse(List<FileUploadDescriptor> descriptors) {
}
