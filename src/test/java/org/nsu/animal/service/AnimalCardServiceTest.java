package org.nsu.animal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.animal.dto.requests.CreateAnimalCardRequest;
import org.nsu.animal.entity.Animal;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.AnimalCardStatus;
import org.nsu.animal.entity.PlacementGoal;
import org.nsu.animal.mapper.AnimalCardMapper;
import org.nsu.animal.repository.AnimalCardFileRepository;
import org.nsu.animal.repository.AnimalCardRepository;
import org.nsu.animal.repository.AnimalCardStatusRepository;
import org.nsu.animal.repository.AnimalRepository;
import org.nsu.animal.repository.PlacementGoalRepository;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.testutils.TestDataFactory;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.users.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AnimalCardServiceTest {

    @Mock
    private AnimalCardRepository animalCardRepository;
    
    @Mock
    private AnimalRepository animalRepository;
    
    @Mock
    private PlacementGoalRepository placementGoalRepository;
    
    @Mock
    private AnimalCardStatusRepository animalCardStatusRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private AnimalCardMapper animalCardMapper;
    
    @Mock
    private AnimalCardFileRepository animalCardFileRepository;
    
    @Mock
    private SecurityContext securityContext;
    
    @Mock
    private Authentication authentication;
    
    @Mock
    private PersonDetails personDetails;

    @InjectMocks
    private AnimalCardService animalCardService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(personDetails);
        when(personDetails.getUserId()).thenReturn(1L);
    }

    @Test
    void createAnimalCard_WithValidData_ShouldCreateSuccessfully() {
        CreateAnimalCardRequest request = TestDataFactory.createValidAnimalCardRequest();
        User user = TestDataFactory.createTestUser();
        Animal animal = TestDataFactory.createTestAnimal();
        PlacementGoal goal = TestDataFactory.createTestPlacementGoal();
        AnimalCardStatus status = TestDataFactory.createTestAnimalCardStatus();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(animalRepository.findById(anyLong())).thenReturn(Optional.of(animal));
        when(placementGoalRepository.findByGoal(anyString())).thenReturn(Optional.of(goal));
        when(animalCardStatusRepository.findByName("ON_CHECKING")).thenReturn(Optional.of(status));
        when(animalCardRepository.save(any(AnimalCard.class))).thenReturn(new AnimalCard());

        animalCardService.createAnimalCard(request);

        verify(animalCardRepository).save(any(AnimalCard.class));
        verify(animalCardMapper).updateAnimalCardFromRequest(any(CreateAnimalCardRequest.class), any(AnimalCard.class));
    }

    @Test
    void createAnimalCard_WithInvalidSpeciesId_ShouldThrowException() {
        CreateAnimalCardRequest request = TestDataFactory.createValidAnimalCardRequest();
        User user = TestDataFactory.createTestUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(animalRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> {
            animalCardService.createAnimalCard(request);
        });
    }

    @Test
    void createAnimalCard_WithSellGoalButNoCost_ShouldThrowException() {
        CreateAnimalCardRequest request = TestDataFactory.createValidAnimalCardRequest();
        request.setCost(null);
        User user = TestDataFactory.createTestUser();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> {
            animalCardService.createAnimalCard(request);
        });
    }
}