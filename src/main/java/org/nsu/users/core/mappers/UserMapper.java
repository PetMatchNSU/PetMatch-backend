package org.nsu.users.core.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.nsu.users.core.dto.responses.positive.UserResponse;
import org.nsu.users.entity.BondTime;
import org.nsu.users.entity.Contact;
import org.nsu.users.entity.Gender;
import org.nsu.users.entity.User;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;

@Mapper(componentModel = "spring")
public interface UserMapper {

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
    UserResponse toUserResponse(User user, String reviewComment, Set<Contact> contacts);

    @Mapping(target = "bondTimeStart", source = "startContactTime", qualifiedByName = "localTimeToOffsetDateTime")
    @Mapping(target = "bondTimeEnd", source = "endContactTime", qualifiedByName = "localTimeToOffsetDateTime")
    UserResponse.BondTimeDto toBondTimeDto(BondTime bondTime);

    @Mapping(target = "type", source = "type.name")
    @Mapping(target = "contact", source = "link")
    @Mapping(target = "visible", source = "isVisible")
    UserResponse.ContactInfoDto toContactInfoDto(Contact contact);

    @Named("genderToString")
    default String genderToString(Gender gender) {
        return gender != null ? gender.name() : null;
    }

    @Named("localTimeToOffsetDateTime")
    default OffsetDateTime localTimeToOffsetDateTime(LocalTime localTime) {
        if (localTime == null) {
            return null;
        }
        ZoneId moscowZone = ZoneId.of("Europe/Moscow");
        ZonedDateTime zdt = ZonedDateTime.now(moscowZone).with(localTime);
        return zdt.toOffsetDateTime();
    }
}