package org.nsu.files.dto;

import java.util.List;

public record DeleteRequest(List<Long> fileIds, List<Long> cardIds) {
}
