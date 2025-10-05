package org.nsu.authorization.core.dto.responses.negative;


import lombok.Getter;
import lombok.ToString;
import org.nsu.authorization.core.dto.responses.AbstractNegativeResponse;

@ToString
@Getter
public class PersonErrorResponse extends AbstractNegativeResponse {

    private final String error;

    public PersonErrorResponse(String error, long timestamp) {
        super(timestamp);
        this.error = error;
    }

}
