package org.nsu.files.dto;

import java.util.List;

public record FilterDTO(
    List<Long> fileIds,
    List<Long> cardIds,
    String isMain,
    List<String> fileTypes
) {
}
