package org.nsu.admin.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.nsu.admin.dto.AdminCardDto;
import org.nsu.admin.dto.AdminCardOwnerDto;
import org.nsu.admin.dto.AdminModerationDto;
import org.nsu.animal.entity.AnimalCard;

@Mapper(componentModel = "spring")
public interface AdminCardMapper {

    @Mapping(target = "cardId", source = "card.id")
    @Mapping(target = "animalId", source = "card.animal.id")
    @Mapping(target = "status", source = "card.status.name")
    @Mapping(target = "goal", source = "card.goal.goal")
    @Mapping(target = "cardName", source = "card.name")
    @Mapping(target = "createdAt", source = "card.created")
    @Mapping(target = "updatedAt", source = "card.updated")
    @Mapping(target = "owner", expression = "java(mapOwner(card))")
    @Mapping(target = "moderation", source = "moderation")
    AdminCardDto toDto(AnimalCard card, AdminModerationDto moderation);

    default AdminCardOwnerDto mapOwner(AnimalCard card) {
        return new AdminCardOwnerDto(
            card.getCardAuthor().getId(),
            card.getCardAuthor().getFirstName(),
            card.getCardAuthor().getSecondName(),
            card.getCardAuthor().getMiddleName()
        );
    }
}
