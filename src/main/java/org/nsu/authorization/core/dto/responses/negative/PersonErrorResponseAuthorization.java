package org.nsu.authorization.core.dto.responses.negative;


import lombok.Getter;
import lombok.ToString;
import org.nsu.authorization.core.dto.responses.AbstractAuthorizationNegativeResponse;

@ToString
@Getter
public class PersonErrorResponseAuthorization extends AbstractAuthorizationNegativeResponse {

    private final String error;

    public PersonErrorResponseAuthorization(String error, long timestamp) {
        super(timestamp);
        this.error = error;
    }

}
