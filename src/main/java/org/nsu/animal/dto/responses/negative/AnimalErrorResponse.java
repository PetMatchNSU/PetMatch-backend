package org.nsu.animal.dto.responses.negative;

import lombok.Getter;
import lombok.ToString;
import common.dto.responses.negative.AbstractNegativeResponse;

@ToString
@Getter
public class AnimalErrorResponse extends AbstractNegativeResponse {

    private final String error;

    public AnimalErrorResponse(String error, long timestamp) {
        super(timestamp);
        this.error = error;
    }
}