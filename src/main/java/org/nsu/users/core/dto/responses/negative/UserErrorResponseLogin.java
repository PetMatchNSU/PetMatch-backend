package org.nsu.users.core.dto.responses.negative;


import lombok.Getter;
import lombok.ToString;
import org.nsu.users.core.dto.AbstractUserNegativeResponse;

@ToString
@Getter
public class UserErrorResponseLogin extends AbstractUserNegativeResponse {

    private final String error;

    public UserErrorResponseLogin(String error, long timestamp) {
        super(timestamp);
        this.error = error;
    }

}
