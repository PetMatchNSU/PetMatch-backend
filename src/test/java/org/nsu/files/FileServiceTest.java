package org.nsu.files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.authorization.core.utils.JWTUtil;
import org.nsu.files.dto.FilterDTO;
import org.nsu.files.dto.MetadataDTO;
import org.nsu.files.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
public class FileServiceTest {

    static {
        // Ensure containers are started before any test
        org.nsu.testutils.TestContainerManager.postgres.start();
        org.nsu.testutils.TestContainerManager.minio.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", org.nsu.testutils.TestContainerManager.postgres::getJdbcUrl);
        registry.add("spring.datasource.username", org.nsu.testutils.TestContainerManager.postgres::getUsername);
        registry.add("spring.datasource.password", org.nsu.testutils.TestContainerManager.postgres::getPassword);
        registry.add("minio.endpoint", org.nsu.testutils.TestContainerManager.minio::getS3URL);
        registry.add("minio.access-key", org.nsu.testutils.TestContainerManager.minio::getUserName);
        registry.add("minio.secret-key", org.nsu.testutils.TestContainerManager.minio::getPassword);
        registry.add("minio.bucket-name", () -> "test-bucket");
    }

    @MockitoBean
    private JWTUtil jwtUtil;

    @Autowired
    private FileService fileService;

    @Test
    public void testGetFilesWithNullFilter() {
        String query = Base64.getEncoder().encodeToString("{}".getBytes());
        MetadataDTO result = fileService.getFiles(query);
        assertNotNull(result);
    }

    @Test
    public void testGetFilesWithFileIds() {
        String query = Base64.getEncoder().encodeToString("{\"fileIds\":[1,2]}".getBytes());
        MetadataDTO result = fileService.getFiles(query);
        assertNotNull(result);
    }

    @Test
    public void testGetFilesWithCardIds() {
        String query = Base64.getEncoder().encodeToString("{\"cardIds\":[1]}".getBytes());
        MetadataDTO result = fileService.getFiles(query);
        assertNotNull(result);
    }

    @Test
    public void testGetFilesWithFileTypes() {
        String query = Base64.getEncoder().encodeToString("{\"fileTypes\":[\"PHOTO\"]}".getBytes());
        MetadataDTO result = fileService.getFiles(query);
        assertNotNull(result);
    }
}
