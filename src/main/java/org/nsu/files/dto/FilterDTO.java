package org.nsu.files.dto;

import java.util.List;

public record FilterDTO(
    List<Long> fileIds,
    List<Long> cardIds,
    Boolean isMain,
    List<String> fileTypes
) {
}
