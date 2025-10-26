package org.nsu.users.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.nsu.users.dto.responses.CitySearchResponse;
import org.nsu.users.entity.Region;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RegionMapper {

    @Mapping(source = "id", target = "locationId")
    @Mapping(source = "region", target = "region")
    @Mapping(source = "city", target = "city")
    CitySearchResponse.LocationDto toLocationDto(Region region);

    List<CitySearchResponse.LocationDto> toLocationDtoList(List<Region> regions);
}