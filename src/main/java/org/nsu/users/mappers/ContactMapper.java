package org.nsu.users.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.nsu.users.dto.requests.UpdateUserRequest;
import org.nsu.users.entity.Contact;

@Mapper(componentModel = "spring")
public interface ContactMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(source = "contact", target = "link")
    @Mapping(source = "visible", target = "isVisible")
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "type", ignore = true)
    Contact toEntity(UpdateUserRequest.ContactInfoDto dto);
}
