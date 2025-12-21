package org.nsu.animal.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.nsu.animal.dto.requests.CreateAnimalCardRequest;
import org.nsu.animal.dto.responses.AnimalOwnerContactsResponse;
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

import static org.junit.jupiter.api.Assertions.*;
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

        AnimalCard savedAnimalCard = new AnimalCard();
        savedAnimalCard.setId(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(animalRepository.findById(anyLong())).thenReturn(Optional.of(animal));
        when(placementGoalRepository.findByGoal(anyString())).thenReturn(Optional.of(goal));
        when(animalCardStatusRepository.findByName("ON_CHECKING")).thenReturn(Optional.of(status));
        when(animalCardRepository.save(any(AnimalCard.class))).thenReturn(savedAnimalCard);

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

    @Nested
    @MockitoSettings(strictness = Strictness.LENIENT)
    class GetOwnerContactsTests {

        @Test
        void getOwnerContacts_WithValidAnimalId_ShouldReturnOwnerContacts() {
            Long animalId = 1L;
            User owner = TestDataFactory.createTestUser();
            owner.setBondTimes(TestDataFactory.createTestBondTimes(owner));
            owner.setContacts(TestDataFactory.createTestContactsList(owner));

            AnimalCard animalCard = TestDataFactory.createTestAnimalCard(owner);

            when(animalCardRepository.findById(animalId)).thenReturn(Optional.of(animalCard));

            AnimalOwnerContactsResponse response = animalCardService.getOwnerContacts(animalId);

            assertNotNull(response);
            assertEquals(owner.getFirstName(), response.getFirstName());
            assertEquals(owner.getSecondName(), response.getSecondName());
            assertEquals(owner.getMiddleName(), response.getMiddleName());
            assertNotNull(response.getBondTime());
            assertNotNull(response.getContactInfo());
        }

        @Test
        void getOwnerContacts_WithInvalidAnimalId_ShouldThrowException() {
            Long invalidId = 999L;

            when(animalCardRepository.findById(invalidId)).thenReturn(Optional.empty());

            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> animalCardService.getOwnerContacts(invalidId)
            );

            assertEquals("Карточка животного не найдена", exception.getMessage());
        }

        @Test
        void getOwnerContacts_ShouldReturnOnlyVisibleContacts() {
            Long animalId = 1L;
            User owner = TestDataFactory.createTestUser();
            owner.setBondTimes(TestDataFactory.createTestBondTimes(owner));
            owner.setContacts(TestDataFactory.createTestContactsList(owner));

            AnimalCard animalCard = TestDataFactory.createTestAnimalCard(owner);

            when(animalCardRepository.findById(animalId)).thenReturn(Optional.of(animalCard));

            AnimalOwnerContactsResponse response = animalCardService.getOwnerContacts(animalId);

            assertTrue(response.getContactInfo().stream()
                    .allMatch(c -> owner.getContacts().stream()
                            .filter(oc -> oc.getType().getName().equals(c.getType()))
                            .findFirst()
                            .map(oc -> oc.getIsVisible())
                            .orElse(false)));
        }

        @Test
        void getOwnerContacts_ShouldReturnSortedBondTimes() {
            Long animalId = 1L;
            User owner = TestDataFactory.createTestUser();
            owner.setBondTimes(TestDataFactory.createTestBondTimes(owner));
            owner.setContacts(TestDataFactory.createTestContactsList(owner));

            AnimalCard animalCard = TestDataFactory.createTestAnimalCard(owner);

            when(animalCardRepository.findById(animalId)).thenReturn(Optional.of(animalCard));

            AnimalOwnerContactsResponse response = animalCardService.getOwnerContacts(animalId);

            if (response.getBondTime().size() > 1) {
                for (int i = 0; i < response.getBondTime().size() - 1; i++) {
                    assertTrue(response.getBondTime().get(i).getBondTimeStart()
                            .isBefore(response.getBondTime().get(i + 1).getBondTimeStart()) ||
                            response.getBondTime().get(i).getBondTimeStart()
                                    .equals(response.getBondTime().get(i + 1).getBondTimeStart()));
                }
            }
        }
    }
}