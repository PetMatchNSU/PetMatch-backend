package org.nsu.users.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.nsu.users.dto.requests.UpdateUserRequest;
import org.nsu.users.entity.BondTime;

@Mapper(componentModel = "spring")
public interface BondTimeMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(source = "bondTimeStart", target = "start")
    @Mapping(source = "bondTimeEnd", target = "end")
    BondTime toEntity(UpdateUserRequest.BondTimeDto dto);
}
