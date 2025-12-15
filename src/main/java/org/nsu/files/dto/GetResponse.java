package org.nsu.files.dto;

import java.util.List;

public record GetResponse(List<GetFileDescriptor> descriptors) {
}
