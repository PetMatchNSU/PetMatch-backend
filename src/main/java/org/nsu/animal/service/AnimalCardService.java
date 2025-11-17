package org.nsu.animal.service;

import lombok.RequiredArgsConstructor;
import org.nsu.animal.dto.requests.CreateAnimalCardRequest;
import org.nsu.animal.dto.requests.UpdateAnimalCardRequest;
import org.nsu.animal.dto.responses.AnimalCardResponse;
import org.nsu.animal.entity.Animal;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.AnimalCardFile;
import org.nsu.animal.entity.AnimalCardStatus;
import org.nsu.animal.entity.PlacementGoal;
import org.nsu.animal.mapper.AnimalCardMapper;
import org.nsu.animal.repository.AnimalCardFileRepository;
import org.nsu.animal.repository.AnimalCardRepository;
import org.nsu.animal.repository.AnimalCardStatusRepository;
import org.nsu.animal.repository.AnimalRepository;
import org.nsu.animal.repository.PlacementGoalRepository;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AnimalCardService {

    private final AnimalCardRepository animalCardRepository;
    private final AnimalRepository animalRepository;
    private final PlacementGoalRepository placementGoalRepository;
    private final AnimalCardStatusRepository animalCardStatusRepository;
    private final UserRepository userRepository;
    private final AnimalCardMapper animalCardMapper;
    private final AnimalCardFileRepository animalCardFileRepository;

    @Transactional
    public void createAnimalCard(CreateAnimalCardRequest request) {
        User currentUser = getCurrentUser();
        
        validateRequest(request);
        
        AnimalCard animalCard = new AnimalCard();
        
        animalCard.setCardAuthor(currentUser);
        
        Animal animal = animalRepository.findById(request.getSpeciesId())
                .orElseThrow(() -> new IllegalArgumentException("Вид животного не найден"));
        animalCard.setAnimal(animal);
        
        animalCardMapper.updateAnimalCardFromRequest(request, animalCard);
        
        if (Boolean.TRUE.equals(request.getHasBreed()) && request.getBreed() != null) {
            animalCard.setBreed(request.getBreed());
        }
        
        PlacementGoal goal = findGoalByName(request.getGoal());
        animalCard.setGoal(goal);
        
        if ("SELL".equals(request.getGoal()) && request.getCost() != null) {
            animalCard.setCost(request.getCost());
        }
        
        LocalDateTime now = LocalDateTime.now();
        animalCard.setCreated(now);
        animalCard.setUpdated(now);
        
        AnimalCardStatus status = animalCardStatusRepository.findByName("ON_CHECKING")
                .orElseThrow(() -> new IllegalStateException("Статус 'ON_CHECKING' не найден в базе данных"));
        animalCard.setStatus(status);
        
        animalCardRepository.save(animalCard);
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        Long userId = personDetails.getUserId();
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
    }
    
    private void validateRequest(CreateAnimalCardRequest request) {
        if (Boolean.TRUE.equals(request.getHasBreed()) &&
            (request.getBreed() == null || request.getBreed().trim().isEmpty())) {
            throw new IllegalArgumentException("Порода должна быть указана, если животное породистое");
        }
        
        if ("SELL".equals(request.getGoal()) && request.getCost() == null) {
            throw new IllegalArgumentException("Стоимость должна быть указана для продажи");
        }
        
        LocalDate minDate = LocalDate.of(1980, 1, 1);
        if (request.getBirthday().isBefore(minDate)) {
            throw new IllegalArgumentException("Дата рождения не может быть раньше 01.01.1980");
        }
        
        if (request.getBirthday().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }
    }
    
    private PlacementGoal findGoalByName(String goalName) {
        return placementGoalRepository.findByGoal(goalName)
                .orElseThrow(() -> new IllegalArgumentException("Цель размещения не найдена: " + goalName));
    }

    @Transactional(readOnly = true)
    public AnimalCardResponse getAnimalCard(Long animalId) {
        AnimalCard animalCard = animalCardRepository.findById(animalId)
                .orElseThrow(() -> new IllegalArgumentException("Карточка животного не найдена"));

        User currentUser = getCurrentUser();
        List<AnimalCardFile> files = animalCardFileRepository.findByAnimalCardId(animalId);
        return animalCardMapper.toResponse(animalCard, currentUser, files);
    }

    @Transactional
    public void updateAnimalCard(Long animalId, UpdateAnimalCardRequest request) {
        AnimalCard animalCard = animalCardRepository.findById(animalId)
                .orElseThrow(() -> new IllegalArgumentException("Карточка животного не найдена"));

        User currentUser = getCurrentUser();
        if (!Objects.equals(animalCard.getCardAuthor().getId(), currentUser.getId())) {
            throw new IllegalArgumentException("Нет прав для редактирования этой карточки");
        }

        validateUpdateRequest(request);

        Animal animal = animalRepository.findById(request.getSpeciesId())
                .orElseThrow(() -> new IllegalArgumentException("Вид животного не найден"));
        animalCard.setAnimal(animal);

        animalCardMapper.updateAnimalCardFromRequest(request, animalCard);

        if (Boolean.TRUE.equals(request.getHasBreed()) && request.getBreed() != null) {
            animalCard.setBreed(request.getBreed());
        } else {
            animalCard.setBreed(null);
        }

        PlacementGoal goal = findGoalByName(request.getGoal());
        animalCard.setGoal(goal);

        if ("SELL".equals(request.getGoal()) && request.getCost() != null) {
            animalCard.setCost(request.getCost());
        } else {
            animalCard.setCost(null);
        }

        animalCard.setUpdated(LocalDateTime.now());

        animalCardRepository.save(animalCard);
    }

    @Transactional
    public void deleteAnimalCard(Long animalId) {
        AnimalCard animalCard = animalCardRepository.findById(animalId)
                .orElseThrow(() -> new IllegalArgumentException("Карточка животного не найдена"));

        User currentUser = getCurrentUser();
        if (!Objects.equals(animalCard.getCardAuthor().getId(), currentUser.getId())) {
            throw new IllegalArgumentException("Нет прав для удаления этой карточки");
        }

        animalCardRepository.delete(animalCard);
    }

    private void validateUpdateRequest(UpdateAnimalCardRequest request) {
        if (Boolean.TRUE.equals(request.getHasBreed()) &&
            (request.getBreed() == null || request.getBreed().trim().isEmpty())) {
            throw new IllegalArgumentException("Порода должна быть указана, если животное породистое");
        }
        
        if ("SELL".equals(request.getGoal()) && request.getCost() == null) {
            throw new IllegalArgumentException("Стоимость должна быть указана для продажи");
        }
        
        LocalDate minDate = LocalDate.of(1980, 1, 1);
        if (request.getBirthday().isBefore(minDate)) {
            throw new IllegalArgumentException("Дата рождения не может быть раньше 01.01.1980");
        }
        
        if (request.getBirthday().isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("Дата рождения не может быть в будущем");
        }
    }

}