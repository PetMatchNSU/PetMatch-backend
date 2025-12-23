package org.nsu.feed.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.feed.dto.util.Location;
import org.nsu.feed.dto.responses.animalList.AnimalCardDto;

@Mapper(componentModel = "spring")
public interface FeedAnimalCardMapper {

    @Mapping(target = "animalId", source = "id")
    @Mapping(target = "speciesName", source = "animal.name")
    @Mapping(target = "goal", source = "goal.goal")
    @Mapping(target = "hasBreed", expression = "java(ac.getBreed() != null && !ac.getBreed().isBlank())")
    @Mapping(target = "birthday", source = "birthdate")
    @Mapping(target = "location", expression = "java(mapLocation(ac))")
    @Mapping(target = "createdAt", source = "created")
    @Mapping(target = "mainPhotoId", ignore = true)
    AnimalCardDto toDto(AnimalCard ac);

    default Location mapLocation(AnimalCard ac) {
        Location loc = new Location();
        if (ac == null || ac.getCardAuthor() == null || ac.getCardAuthor().getRegion() == null) {
            return loc;
        }
        if (ac.getCardAuthor().getRegion().getCity() != null) {
            loc.setCity(ac.getCardAuthor().getRegion().getCity());
        }
        if (ac.getCardAuthor().getRegion().getRegion() != null) {
            loc.setRegion(ac.getCardAuthor().getRegion().getRegion());
        }
        return loc;
    }
}
