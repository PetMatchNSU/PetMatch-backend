package org.nsu.users.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.nsu.users.dto.responses.ContactInfoResponse;
import org.nsu.users.entity.Contact;
import org.nsu.users.entity.ContactType;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ContactInfoMapper {

    @Mapping(source = "type.name", target = "type")
    @Mapping(source = "link", target = "contact")
    ContactInfoResponse.ContactInfoDto toContactInfoDto(Contact contact);

    List<ContactInfoResponse.ContactInfoDto> toContactInfoDtoList(List<Contact> contacts);
}