package org.nsu.users.mapping;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.nsu.authorization.core.dto.requests.registrationRequest.RegistrationRequest;
import org.nsu.users.entity.User;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "region", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "bondTimes", ignore = true)
    User toUser(RegistrationRequest request);
}
