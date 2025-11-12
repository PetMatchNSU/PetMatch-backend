package org.nsu.users.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.nsu.users.dto.responses.ContactTypeResponse;
import org.nsu.users.entity.ContactType;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContactTypeMapper {

    @Mapping(source = "id", target = "id")
    @Mapping(source = "name", target = "type")
    ContactTypeResponse.ContactTypeDto toContactTypeDto(ContactType contactType);

    List<ContactTypeResponse.ContactTypeDto> toContactTypeDtoList(List<ContactType> contactTypes);
}