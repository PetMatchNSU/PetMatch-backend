package org.nsu.feed.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.nsu.animal.entity.Animal;
import org.nsu.animal.entity.PlacementGoal;
import org.nsu.feed.dto.responses.AnimalInfoResponse;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AnimalInfoMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "name")
    AnimalInfoResponse.Species toSpeciesDto(Animal animal);

    List<AnimalInfoResponse.Species> toSpeciesDtoList(List<Animal> animals);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "name", source = "goal")
    AnimalInfoResponse.Goal toGoalDto(PlacementGoal goal);

    List<AnimalInfoResponse.Goal> toGoalDtoList(List<PlacementGoal> goals);
}
