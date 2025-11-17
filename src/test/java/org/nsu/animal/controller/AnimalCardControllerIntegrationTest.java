package org.nsu.animal.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.nsu.animal.dto.requests.CreateAnimalCardRequest;
import org.nsu.animal.dto.requests.UpdateAnimalCardRequest;
import org.nsu.testutils.AbstractIntegrityTest;
import org.nsu.testutils.TestDataFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureWebMvc
class AnimalCardControllerIntegrationTest extends AbstractIntegrityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void createAnimalCard_WithValidData_ShouldReturnOk() throws Exception {
        CreateAnimalCardRequest request = TestDataFactory.createValidAnimalCardRequest();

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
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void getAnimalCard_WithValidId_ShouldReturnAnimalCard() throws Exception {
        Long animalId = 1L;

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

        mockMvc.perform(get("/api/v1/animals/show/{animalId}", invalidId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getAnimalCard_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        Long animalId = 1L;

        mockMvc.perform(get("/api/v1/animals/show/{animalId}", animalId))
                .andExpect(status().isUnauthorized());
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
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void deleteAnimalCard_WithValidId_ShouldReturnOk() throws Exception {
        Long animalId = 1L;

        mockMvc.perform(delete("/api/v1/animals/{animalId}", animalId))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test@example.com", authorities = {"USER"})
    void deleteAnimalCard_WithInvalidId_ShouldReturnBadRequest() throws Exception {
        Long invalidId = 999L;

        mockMvc.perform(delete("/api/v1/animals/{animalId}", invalidId))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteAnimalCard_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        Long animalId = 1L;

        mockMvc.perform(delete("/api/v1/animals/{animalId}", animalId))
                .andExpect(status().isUnauthorized());
    }
}