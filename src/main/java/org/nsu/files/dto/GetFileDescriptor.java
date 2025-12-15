package org.nsu.files.dto;

public record GetFileDescriptor(
    String file_id,
    String file_type,
    boolean is_main,
    String original_filename,
    String card_id,
    String content
) {
}
