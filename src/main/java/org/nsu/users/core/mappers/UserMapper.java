package org.nsu.users.core.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.core.services.TimezoneService;
import org.nsu.users.entity.BondTime;
import org.nsu.users.entity.Contact;
import org.nsu.users.entity.Gender;
import org.nsu.users.entity.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING, uses = TimezoneService.class)
public abstract class UserMapper {

    @Autowired
    protected TimezoneService timezoneService;

    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "secondName", source = "user.secondName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "gender", source = "user.gender", qualifiedByName = "genderToString")
    @Mapping(target = "region", source = "user.region.region")
    @Mapping(target = "city", source = "user.region.city")
    @Mapping(target = "reviewStatus", source = "user.status.name")
    @Mapping(target = "reviewComment", source = "reviewComment")
    @Mapping(target = "bondTime", source = "user.bondTimes")
    @Mapping(target = "contactInfo", source = "contacts")
     public abstract UserResponse toUserResponse(User user, String reviewComment, Set<Contact> contacts);

    @Mapping(target = "bondTimeStart", source = "startContactTime", qualifiedByName = "localTimeToOffsetDateTime")
    @Mapping(target = "bondTimeEnd", source = "endContactTime", qualifiedByName = "localTimeToOffsetDateTime")
    public abstract UserResponse.BondTimeDto toBondTimeDto(BondTime bondTime);

    @Mapping(target = "type", source = "type.name")
    @Mapping(target = "contact", source = "link")
    @Mapping(target = "visible", source = "isVisible")
    public abstract UserResponse.ContactInfoDto toContactInfoDto(Contact contact);

    @Named("genderToString")
    protected String genderToString(Gender gender) {
        return gender != null ? gender.name() : null;
    }

    @Named("localTimeToOffsetDateTime")
    protected OffsetDateTime localTimeToOffsetDateTime(LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        return timezoneService.convertLocalTimeToOffsetDateTime(localTime);
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "bondTimes", ignore = true)
    public abstract User toUser(RegistrationRequest request);
}
