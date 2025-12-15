package org.nsu.files.dto;

import java.util.List;

public record DeleteResponse(List<DeleteDescriptor> descriptors) {
}
