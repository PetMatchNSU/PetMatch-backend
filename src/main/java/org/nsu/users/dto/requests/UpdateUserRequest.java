package org.nsu.users.dto.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UpdateUserRequest {

    @NotBlank
    private String firstName;

    @NotBlank
    private String secondName;

    private String lastName;

    @NotBlank // "M"/"F"
    private String gender;

    @NotNull
    private Long locationId;

    @Valid
    private List<BondTimeDto> bondTime;

    @Valid
    private List<ContactInfoDto> contactInfo;

    @Getter
    @Setter
    public static class BondTimeDto {
        @NotBlank
        private String bondTimeStart; // "HH:mm"
        @NotBlank
        private String bondTimeEnd;   // "HH:mm"
    }

    @Getter
    @Setter
    public static class ContactInfoDto {
        @NotBlank
        private String type;   // ContactType.name
        @NotBlank
        private String contact; // Contact.link
        @NotNull
        private Boolean visible; // Contact.isVisible
    }
}