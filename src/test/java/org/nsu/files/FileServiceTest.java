package org.nsu.files;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.nsu.authorization.core.utils.JWTUtil;
import org.nsu.files.dto.FilterDTO;
import org.nsu.files.dto.MetadataDTO;
import org.nsu.files.service.FileService;
import org.nsu.testutils.AbstractIntegrityTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;

import java.util.List;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@ActiveProfiles("test")
@SpringBootTest()
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.mail.MailSenderAutoConfiguration"
})
@AutoConfigureMockMvc
public class FileServiceTest extends AbstractIntegrityTest {

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
