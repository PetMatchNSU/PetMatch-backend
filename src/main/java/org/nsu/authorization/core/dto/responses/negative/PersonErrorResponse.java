package org.nsu.authorization.core.dto.responses.negative;


import org.nsu.authorization.core.dto.responses.AbstractNegativeResponse;

import java.util.Map;

public class PersonErrorResponse extends AbstractNegativeResponse {

    public PersonErrorResponse(Map<String, Object> errors, long timestamp) {
        super(errors, timestamp);
    }

}
