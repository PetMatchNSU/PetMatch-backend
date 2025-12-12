package org.nsu.animal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.nsu.animal.dto.requests.CreateAnimalCardRequest;
import org.nsu.animal.dto.requests.UpdateAnimalCardRequest;
import org.nsu.animal.dto.responses.AnimalCardResponse;
import org.nsu.animal.service.AnimalCardService;
import org.nsu.authorization.core.exceptions.handlers.GlobalExceptionHandler;
import org.nsu.testutils.TestDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AnimalCardController.class, excludeFilters = {
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.config.*"),
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "org.nsu.authorization.core.security.*"),
})
@ContextConfiguration(classes = {AnimalCardController.class, AnimalCardTestSecurityConfig.class,
        org.nsu.animal.exceptions.AnimalCardExceptionHandler.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
                + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration"
})
class AnimalCardControllerIntegrationTest {

    @MockBean
    private AnimalCardService animalCardService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void createAnimalCard_WithValidData_ShouldReturnOk() throws Exception {
        CreateAnimalCardRequest request = TestDataFactory.createValidAnimalCardRequest();
        
        doNothing().when(animalCardService).createAnimalCard(any(CreateAnimalCardRequest.class));

        mockMvc.perform(post("/api/v1/animals/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void createAnimalCard_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        CreateAnimalCardRequest request = TestDataFactory.createInvalidAnimalCardRequest();

        mockMvc.perform(post("/api/v1/animals/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void createAnimalCard_WithoutBreed_ShouldReturnOk() throws Exception {
        CreateAnimalCardRequest request = TestDataFactory.createAnimalCardRequestWithoutBreed();
        
        doNothing().when(animalCardService).createAnimalCard(any(CreateAnimalCardRequest.class));

        mockMvc.perform(post("/api/v1/animals/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void createAnimalCard_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        CreateAnimalCardRequest request = TestDataFactory.createValidAnimalCardRequest();

        mockMvc.perform(post("/api/v1/animals/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void getAnimalCard_WithValidId_ShouldReturnAnimalCard() throws Exception {
        Long animalId = 1L;
        
        AnimalCardResponse.SpeciesDto speciesDto = new AnimalCardResponse.SpeciesDto(1L, "Dog");
        AnimalCardResponse.PhotosDto photosDto = new AnimalCardResponse.PhotosDto(null, List.of());
        AnimalCardResponse.DocumentsDto documentsDto = new AnimalCardResponse.DocumentsDto(null, null, null, null, null);
        
        AnimalCardResponse mockResponse = new AnimalCardResponse(
                true, // canEdit
                "Test Animal", // name
                speciesDto, // species
                "SELL", // goal
                BigDecimal.valueOf(1000), // cost
                "Test Breed", // breed
                org.nsu.animal.dto.enums.Gender.M, // gender
                LocalDate.now().minusYears(1), // birthday
                BigDecimal.valueOf(5.0), // weight
                "Brown", // color
                "No diseases", // geneticDiseases
                "Test description", // description
                "PUBLISHED", // reviewStatus
                photosDto, // photos
                documentsDto, // documents
                LocalDateTime.now(), // createdAt
                LocalDateTime.now() // updatedAt
        );
        
        when(animalCardService.getAnimalCard(animalId)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/v1/animals/show/{animalId}", animalId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.species").exists())
                .andExpect(jsonPath("$.canEdit").exists());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void getAnimalCard_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        Long invalidId = 999L;
        
        when(animalCardService.getAnimalCard(invalidId))
                .thenThrow(new IllegalArgumentException("Карточка животного не найдена"));

        mockMvc.perform(get("/api/v1/animals/show/{animalId}", invalidId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAnimalCard_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        Long animalId = 1L;

        mockMvc.perform(get("/api/v1/animals/show/{animalId}", animalId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void updateAnimalCard_WithValidData_ShouldReturnOk() throws Exception {
        Long animalId = 1L;
        UpdateAnimalCardRequest request = TestDataFactory.createValidUpdateAnimalCardRequest();

        mockMvc.perform(put("/api/v1/animals/update")
                        .param("animalId", animalId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void updateAnimalCard_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        Long animalId = 1L;
        UpdateAnimalCardRequest request = TestDataFactory.createInvalidUpdateAnimalCardRequest();

        mockMvc.perform(put("/api/v1/animals/update")
                        .param("animalId", animalId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void updateAnimalCard_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        Long invalidId = 999L;
        UpdateAnimalCardRequest request = TestDataFactory.createValidUpdateAnimalCardRequest();
        
        doThrow(new IllegalArgumentException("Карточка животного не найдена"))
                .when(animalCardService).updateAnimalCard(eq(invalidId), any(UpdateAnimalCardRequest.class));

        mockMvc.perform(put("/api/v1/animals/update")
                        .param("animalId", invalidId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAnimalCard_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        Long animalId = 1L;
        UpdateAnimalCardRequest request = TestDataFactory.createValidUpdateAnimalCardRequest();

        mockMvc.perform(put("/api/v1/animals/update")
                        .param("animalId", animalId.toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void deleteAnimalCard_WithValidId_ShouldReturnOk() throws Exception {
        Long animalId = 1L;
        
        doNothing().when(animalCardService).deleteAnimalCard(animalId);

        mockMvc.perform(delete("/api/v1/animals/{animalId}", animalId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void deleteAnimalCard_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        Long invalidId = 999L;
        
        doThrow(new IllegalArgumentException("Карточка животного не найдена"))
                .when(animalCardService).deleteAnimalCard(invalidId);

        mockMvc.perform(delete("/api/v1/animals/{animalId}", invalidId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAnimalCard_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        Long animalId = 1L;

        mockMvc.perform(delete("/api/v1/animals/{animalId}", animalId))
                .andExpect(status().isForbidden());
    }
}
