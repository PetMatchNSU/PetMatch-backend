package org.nsu.users.services;

import lombok.RequiredArgsConstructor;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.AnimalCardFile;
import org.nsu.animal.repository.AnimalCardFileRepository;
import org.nsu.animal.repository.AnimalCardRepository;
import org.nsu.users.dto.responses.UserAnimalListResponse;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserAnimalListService {

    private static final String PHOTO_FILE_TYPE = "photo";

    private final AnimalCardRepository animalCardRepository;
    private final AnimalCardFileRepository animalCardFileRepository;

    public UserAnimalListResponse getUserAnimalList(Long userId) {

        List<AnimalCard> cards = animalCardRepository.findByCardAuthorIdOrderByCreatedDesc(userId);

        if (cards.isEmpty()) {
            return new UserAnimalListResponse(Collections.emptyList());
        }

        List<Long> cardIds = cards.stream()
                .map(AnimalCard::getId)
                .collect(Collectors.toList());

        List<AnimalCardFile> allFiles = animalCardFileRepository.findByAnimalCardIdIn(cardIds);

        Map<Long, List<AnimalCardFile>> filesMap = allFiles.stream()
                .collect(Collectors.groupingBy(file -> file.getAnimalCard().getId()));

        List<UserAnimalListResponse.Animal> animals = cards.stream().map(card -> {
            List<AnimalCardFile> files = filesMap.getOrDefault(card.getId(), Collections.emptyList());

            Long mainPhotoId = files.stream()
                    .filter(acf -> acf.getFileType() != null
                            && PHOTO_FILE_TYPE.equalsIgnoreCase(acf.getFileType().getName())
                            && acf.getFile() != null)
                    .map(acf -> acf.getFile().getId())
                    .findFirst()
                    .orElse(null);

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
                    null);
        }).collect(Collectors.toList());

        return new UserAnimalListResponse(animals);
    }
}