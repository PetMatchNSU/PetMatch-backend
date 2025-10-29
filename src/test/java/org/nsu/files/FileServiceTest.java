package org.nsu.files;

import org.junit.jupiter.api.Test;
import org.nsu.authorization.core.utils.JWTUtil;
import org.nsu.files.dto.FilterDTO;
import org.nsu.files.dto.MetadataDTO;
import org.nsu.files.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
public class FileServiceTest extends AbstractIntegrationTest {

    @MockitoBean
    private JWTUtil jwtUtil;

    @Autowired
    private FileService fileService;

    @Test
    public void testGetFilesWithNullFilter() {
        String query = "{}";
        MetadataDTO result = fileService.getFiles(query);
        assertNotNull(result);
    }

    @Test
    public void testGetFilesWithFileIds() {
        String query = "{\"fileIds\":[1,2]}";
        MetadataDTO result = fileService.getFiles(query);
        assertNotNull(result);
    }

    @Test
    public void testGetFilesWithCardIds() {
        String query = "{\"cardIds\":[1]}";
        MetadataDTO result = fileService.getFiles(query);
        assertNotNull(result);
    }

    @Test
    public void testGetFilesWithFileTypes() {
        String query = "{\"fileTypes\":[\"PHOTO\"]}";
        MetadataDTO result = fileService.getFiles(query);
        assertNotNull(result);
    }
}
