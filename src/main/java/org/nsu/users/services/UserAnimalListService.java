package org.nsu.users.services;

import lombok.RequiredArgsConstructor;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.AnimalCardFile;
import org.nsu.animal.repository.AnimalCardFileRepository;
import org.nsu.animal.repository.AnimalCardRepository;
import org.nsu.users.dto.requests.UserAnimalListRequest;
import org.nsu.users.dto.responses.UserAnimalListResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAnimalListService {

    private final AnimalCardRepository animalCardRepository;
    private final AnimalCardFileRepository animalCardFileRepository;

    public UserAnimalListResponse getUserAnimalList(UserAnimalListRequest dto) {
        if (dto == null || dto.getUserId() == null) {
            return new UserAnimalListResponse(List.of());
        }

        List<AnimalCard> cards = animalCardRepository.findByCardAuthorIdOrderByCreatedDesc(dto.getUserId());

        List<UserAnimalListResponse.Animal> animals = cards.stream().map(card -> {
            Long mainPhotoId = null;
            List<AnimalCardFile> files = animalCardFileRepository.findByAnimalCardId(card.getId());
            if (files != null) {
                for (AnimalCardFile acf : files) {
                    if (acf.getFileType() != null && "photo".equalsIgnoreCase(acf.getFileType().getName())) {
                        if (acf.getFile() != null) {
                            mainPhotoId = acf.getFile().getId();
                            break;
                        }
                    }
                }
            }

            String speciesName = card.getAnimal() != null ? card.getAnimal().getName() : null;
            String goal = card.getGoal() != null ? card.getGoal().getGoal() : null;
            String reviewStatus = card.getStatus() != null ? card.getStatus().getName() : null;

            return new UserAnimalListResponse.Animal(
                    card.getId(),
                    card.getName(),
                    speciesName,
                    goal,
                    card.getBreed(),
                    card.getGender(),
                    card.getBirthdate(),
                    mainPhotoId,
                    reviewStatus,
                    null
            );
        }).collect(Collectors.toList());

        return new UserAnimalListResponse(animals);
    }
}
