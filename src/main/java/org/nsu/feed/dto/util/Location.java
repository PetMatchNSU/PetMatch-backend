package org.nsu.feed.dto.util;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Location {
    @Schema(description = "регион проживания", example = "Московская область")
    private String region;

    @Schema(description = "город проживания", example = "Москва")
    private String city;
}
