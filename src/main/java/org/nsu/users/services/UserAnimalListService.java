package org.nsu.users.services;

import lombok.RequiredArgsConstructor;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.AnimalCardFile;
import org.nsu.animal.repository.AnimalCardFileRepository;
import org.nsu.animal.repository.AnimalCardRepository;
import org.nsu.users.dto.responses.UserAnimalListResponse;
import org.nsu.users.dto.responses.util.Goal;
import org.nsu.users.dto.responses.util.ReviewStatus;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAnimalListService {

    private static final String PHOTO_FILE_TYPE = "photo";

    private final AnimalCardRepository animalCardRepository;
    private final AnimalCardFileRepository animalCardFileRepository;

    public UserAnimalListResponse getUserAnimalList(Long userId) {

        List<AnimalCard> cards = animalCardRepository.findByCardAuthorIdOrderByCreatedDesc(userId);

        List<UserAnimalListResponse.Animal> animals = cards.stream().map(card -> {
            List<AnimalCardFile> files = animalCardFileRepository.findByAnimalCardId(card.getId());
            Long mainPhotoId = (files == null ? Collections.<AnimalCardFile>emptyList() : files).stream()
                    .filter(acf -> acf.getFileType() != null 
                            && PHOTO_FILE_TYPE.equalsIgnoreCase(acf.getFileType().getName())
                            && acf.getFile() != null)
                    .map(acf -> acf.getFile().getId())
                    .findFirst()
                    .orElse(null);

            String speciesName = card.getAnimal() != null ? card.getAnimal().getName() : null;
            Goal goal = card.getGoal() != null ? Goal.valueOf(card.getGoal().getGoal()) : null;
            ReviewStatus reviewStatus = card.getStatus() != null ? ReviewStatus.valueOf(card.getStatus().getName()) : null;

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
