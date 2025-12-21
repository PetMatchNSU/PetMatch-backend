package org.nsu.animal.service;

import lombok.RequiredArgsConstructor;
import org.nsu.animal.dto.requests.CreateAnimalCardRequest;
import org.nsu.animal.dto.requests.UpdateAnimalCardRequest;
import org.nsu.animal.dto.responses.AnimalCardResponse;
import org.nsu.animal.dto.responses.AnimalOwnerContactsResponse;
import org.nsu.animal.dto.responses.CreateAnimalCardResponse;
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
import org.nsu.users.entity.BondTime;
import org.nsu.users.entity.Contact;
import org.nsu.users.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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

    public CreateAnimalCardResponse createAnimalCard(CreateAnimalCardRequest request) {
        User currentUser = getCurrentUser();
        validateRequest(request);
        
        Animal animal = getAnimalById(request.getSpeciesId());
        PlacementGoal goal = findGoalByName(request.getGoal());
        AnimalCardStatus status = getDefaultStatus();
        
        AnimalCard animalCard = buildAnimalCard(request, currentUser, animal, goal, status);
        
        AnimalCard savedAnimalCard = saveAnimalCard(animalCard);
        
        return new CreateAnimalCardResponse(savedAnimalCard.getId());
    }
    
    private Animal getAnimalById(Long speciesId) {
        return animalRepository.findById(speciesId)
                .orElseThrow(() -> new IllegalArgumentException("Вид животного не найден"));
    }
    
    private AnimalCardStatus getDefaultStatus() {
        return animalCardStatusRepository.findByName("ON_CHECKING")
                .orElseThrow(() -> new IllegalStateException("Статус 'ON_CHECKING' не найден в базе данных"));
    }
    
    private AnimalCard buildAnimalCard(CreateAnimalCardRequest request, User currentUser,
                                      Animal animal, PlacementGoal goal, AnimalCardStatus status) {
        AnimalCard animalCard = new AnimalCard();
        
        animalCard.setCardAuthor(currentUser);
        animalCard.setAnimal(animal);
        animalCard.setGoal(goal);
        animalCard.setStatus(status);
        
        animalCardMapper.updateAnimalCardFromRequest(request, animalCard);
        
        setBreedIfPresent(animalCard, request.getBreed());
        
        setCostIfSelling(animalCard, request.getGoal(), request.getCost());
        
        LocalDateTime now = LocalDateTime.now();
        animalCard.setCreated(now);
        animalCard.setUpdated(now);
        
        return animalCard;
    }
    
    private void setBreedIfPresent(AnimalCard animalCard, String breed) {
        if (breed != null && !breed.trim().isEmpty()) {
            animalCard.setBreed(breed.trim());
        } else {
            animalCard.setBreed(null);
        }
    }
    
    private void setCostIfSelling(AnimalCard animalCard, String goal, java.math.BigDecimal cost) {
        if ("SELL".equals(goal) && cost != null) {
            animalCard.setCost(cost);
        }
    }
    
    @Transactional
    private AnimalCard saveAnimalCard(AnimalCard animalCard) {
        return animalCardRepository.save(animalCard);
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
        Long userId = personDetails.getUserId();
        
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Пользователь не найден"));
    }
    
    private void validateRequest(CreateAnimalCardRequest request) {
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

    public void updateAnimalCard(Long animalId, UpdateAnimalCardRequest request) {
        User currentUser = getCurrentUser();
        validateUpdateRequest(request);
        
        Animal animal = getAnimalById(request.getSpeciesId());
        PlacementGoal goal = findGoalByName(request.getGoal());
        
        updateAnimalCardInTransaction(animalId, request, currentUser, animal, goal);
    }
    
    @Transactional
    private void updateAnimalCardInTransaction(Long animalId, UpdateAnimalCardRequest request,
                                             User currentUser, Animal animal, PlacementGoal goal) {
        AnimalCard animalCard = animalCardRepository.findById(animalId)
                .orElseThrow(() -> new IllegalArgumentException("Карточка животного не найдена"));

        if (!Objects.equals(animalCard.getCardAuthor().getId(), currentUser.getId())) {
            throw new IllegalArgumentException("Нет прав для редактирования этой карточки");
        }

        updateAnimalCardData(animalCard, request, animal, goal);
        
        animalCardRepository.save(animalCard);
    }
    
    private void updateAnimalCardData(AnimalCard animalCard, UpdateAnimalCardRequest request,
                                    Animal animal, PlacementGoal goal) {
        animalCard.setAnimal(animal);
        animalCard.setGoal(goal);
        
        animalCardMapper.updateAnimalCardFromRequest(request, animalCard);
        
        setBreedIfPresent(animalCard, request.getBreed());
        
        updateCostForGoal(animalCard, request.getGoal(), request.getCost());
        
        animalCard.setUpdated(LocalDateTime.now());
    }
    
    private void updateCostForGoal(AnimalCard animalCard, String goal, java.math.BigDecimal cost) {
        if ("SELL".equals(goal) && cost != null) {
            animalCard.setCost(cost);
        } else {
            animalCard.setCost(null);
        }
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

    @Transactional(readOnly = true)
    public AnimalOwnerContactsResponse getOwnerContacts(Long animalId) {
        AnimalCard animalCard = animalCardRepository.findById(animalId)
                .orElseThrow(() -> new IllegalArgumentException("Карточка животного не найдена"));

        User owner = animalCard.getCardAuthor();
        if (owner == null) {
            throw new IllegalArgumentException("Владелец питомца не найден");
        }

        List<AnimalOwnerContactsResponse.BondTimeDto> bondTimeDtos = owner.getBondTimes().stream()
                .sorted(Comparator.comparing(BondTime::getStartContactTime))
                .map(bt -> new AnimalOwnerContactsResponse.BondTimeDto(
                        bt.getStartContactTime(),
                        bt.getEndContactTime()))
                .collect(Collectors.toList());

        List<AnimalOwnerContactsResponse.ContactInfoDto> contactDtos = owner.getContacts().stream()
                .filter(Contact::getIsVisible)
                .map(c -> new AnimalOwnerContactsResponse.ContactInfoDto(
                        c.getType().getName(),
                        c.getLink()))
                .collect(Collectors.toList());

        return new AnimalOwnerContactsResponse(
                owner.getFirstName(),
                owner.getSecondName(),
                owner.getMiddleName(),
                bondTimeDtos,
                contactDtos
        );
    }

}
