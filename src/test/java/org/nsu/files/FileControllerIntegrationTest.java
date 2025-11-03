package org.nsu.files;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.nsu.animal.entity.Animal;
import org.nsu.animal.entity.AnimalCard;
import org.nsu.animal.entity.AnimalCardFileType;
import org.nsu.animal.entity.AnimalCardStatus;
import org.nsu.animal.entity.AnimalGender;
import org.nsu.animal.entity.PlacementGoal;
import org.nsu.animal.repository.AnimalCardFileTypeRepository;
import org.nsu.animal.repository.AnimalCardRepository;
import org.nsu.animal.repository.AnimalRepository;
import org.nsu.animal.repository.AnimalCardStatusRepository;
import org.nsu.animal.repository.PlacementGoalRepository;
import org.nsu.users.core.repositories.UserRepository;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.authorization.core.utils.JWTUtil;
import org.nsu.files.dto.FileDescriptor;
import org.nsu.files.dto.MetadataDTO;
import org.nsu.files.entity.FileType;
import org.nsu.files.repository.FileTypeRepository;
import org.nsu.users.entity.Gender;
import org.nsu.users.entity.Region;
import org.nsu.users.entity.Status;
import org.nsu.users.entity.User;
import org.nsu.users.repository.RegionRepository;
import org.nsu.users.repository.StatusRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockPart;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
public class FileControllerIntegrationTest extends AbstractIntegrationTest {

    @MockitoBean
    private JWTUtil jwtUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StatusRepository statusRepository;

    @Autowired
    private RegionRepository regionRepository;

    @Autowired
    private AnimalCardRepository animalCardRepository;

    @Autowired
    private AnimalRepository animalRepository;

    @Autowired
    private FileTypeRepository fileTypeRepository;

    @Autowired
    private AnimalCardFileTypeRepository animalCardFileTypeRepository;

    @Autowired
    private AnimalCardStatusRepository animalCardStatusRepository;

    @Autowired
    private PlacementGoalRepository placementGoalRepository;

    @BeforeEach
    public void setUp() {
        // Удаляем в правильном порядке, от зависимых к справочным
        animalCardRepository.deleteAll();
        animalRepository.deleteAll();
        placementGoalRepository.deleteAll();
        animalCardStatusRepository.deleteAll();
        fileTypeRepository.deleteAll();
        animalCardFileTypeRepository.deleteAll();
        userRepository.deleteAll();
        statusRepository.deleteAll();
        regionRepository.deleteAll();

        // Создаём и сохраняем справочники
        Status status = new Status();
        status.setName("Active");
        statusRepository.save(status);

        Region region = new Region();
        region.setRegion("Test Region");
        region.setCity("Test City");
        regionRepository.save(region);

        // Создаём пользователя
        User user = new User();
        user.setEmail("test@example.com");
        user.setFirstName("Test");
        user.setSecondName("User");
        user.setLastName("Testovich");
        user.setPassword("password");
        user.setGender(Gender.M);
        user.setEmailVerified(true);
        user.setStatus(status);
        user.setRegion(region);
        userRepository.save(user);

        // Создаём животное
        Animal animal = new Animal();
        animal.setName("Dog");
        animalRepository.save(animal);

        // Создаём цель
        PlacementGoal goal = new PlacementGoal();
        goal.setGoal("Adoption");
        placementGoalRepository.save(goal);

        // Создаём статус карточки
        AnimalCardStatus cardStatus = new AnimalCardStatus();
        cardStatus.setName("Active");
        animalCardStatusRepository.save(cardStatus);

        // Создаём карточку животного
        AnimalCard animalCard = new AnimalCard();
        animalCard.setCardAuthor(user);
        animalCard.setAnimal(animal);
        animalCard.setName("Test Animal Card");
        animalCard.setBreed("Test Breed");
        animalCard.setGender(AnimalGender.M);
        animalCard.setBirthdate(LocalDate.now().minusYears(2));
        animalCard.setWeight(BigDecimal.valueOf(10.5));
        animalCard.setColor("Brown");
        animalCard.setGeneticDiseases("None");
        animalCard.setDescription("Test description");
        animalCard.setGoal(goal);
        animalCard.setCost(BigDecimal.valueOf(100.0));
        animalCard.setCreated(LocalDateTime.now());
        animalCard.setUpdated(LocalDateTime.now());
        animalCard.setStatus(cardStatus);
        animalCardRepository.save(animalCard);

        // Создаём типы файлов
        FileType photoType = new FileType();
        photoType.setName("photo");
        fileTypeRepository.save(photoType);

        AnimalCardFileType cardFileType = new AnimalCardFileType();
        cardFileType.setName("photo");
        animalCardFileTypeRepository.save(cardFileType);
    }


    @Test
    public void testGetFilesEndpoint() throws Exception {
        // Create a mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        PersonDetails personDetails = new PersonDetails(mockUser);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Test the GET /api/v1/files endpoint
        String filterJson = "{}";
        String encodedQuery = Base64.getEncoder().encodeToString(filterJson.getBytes());
        mockMvc.perform(MockMvcRequestBuilders.get("/api/v1/files")
                .param("query", encodedQuery))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    public void testUploadFilesEndpointSuccess() throws Exception {
        // Create a mock user
        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@example.com");

        PersonDetails personDetails = new PersonDetails(mockUser);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(personDetails, null, personDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Create mock file
        MockMultipartFile mockFile = new MockMultipartFile("files", "test.jpg", "image/jpeg", "test content".getBytes());

        // Create metadata
        FileDescriptor descriptor = new FileDescriptor("test.jpg", true, FileDescriptor.FileType.PHOTO, null, null, null, null, null);
        MetadataDTO metadata = new MetadataDTO(List.of(descriptor));
        String metadataJson = objectMapper.writeValueAsString(metadata);

        MockPart metadataPart = new MockPart("metadata", metadataJson.getBytes(StandardCharsets.UTF_8));
        metadataPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        MockPart adIdPart = new MockPart("adId", "1".getBytes(StandardCharsets.UTF_8));
        adIdPart.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        // Perform the request
        mockMvc.perform(MockMvcRequestBuilders.multipart("/api/v1/files/upload")
                .file(mockFile)
                .part(metadataPart)
                .part(adIdPart)
                .contentType(MediaType.MULTIPART_FORM_DATA))
            .andExpect(MockMvcResultMatchers.status().isOk());
    }
}
