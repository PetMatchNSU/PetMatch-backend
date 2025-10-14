package org.nsu.authorization.core.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public abstract class AbstractNegativeResponse {

    private long timestamp;

}


