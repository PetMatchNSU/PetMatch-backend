package org.nsu.authorization.core.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@AllArgsConstructor
public abstract class AbstractNegativeResponse {

    private Map<String, Object> errors;
    private long timestamp;

}


