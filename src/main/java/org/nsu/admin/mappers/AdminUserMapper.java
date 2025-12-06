package org.nsu.admin.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.nsu.admin.dto.AdminModerationDto;
import org.nsu.admin.dto.AdminUserDto;
import org.nsu.users.entity.User;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {

    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "status", source = "user.status.name")
    @Mapping(target = "firstName", source = "user.firstName")
    @Mapping(target = "secondName", source = "user.secondName")
    @Mapping(target = "lastName", source = "user.lastName")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "moderation", source = "moderation")
    AdminUserDto toDto(User user, AdminModerationDto moderation);
}
