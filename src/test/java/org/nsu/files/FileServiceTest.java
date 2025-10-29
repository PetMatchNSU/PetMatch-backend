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
public class FileServiceTest {

    @MockitoBean
    private JWTUtil jwtUtil;

    @Autowired
    private FileService fileService;

    @Test
    public void testGetFilesWithNullFilter() {
        FilterDTO filter = new FilterDTO(null, null, null, null);
        MetadataDTO result = fileService.getFiles(filter);
        assertNotNull(result);
    }

    @Test
    public void testGetFilesWithFileIds() {
        FilterDTO filter = new FilterDTO(List.of(1L, 2L), null, null, null);
        MetadataDTO result = fileService.getFiles(filter);
        assertNotNull(result);
    }

    @Test
    public void testGetFilesWithCardIds() {
        FilterDTO filter = new FilterDTO(null, List.of(1L), null, null);
        MetadataDTO result = fileService.getFiles(filter);
        assertNotNull(result);
    }

    @Test
    public void testGetFilesWithFileTypes() {
        FilterDTO filter = new FilterDTO(null, null, null, List.of("PHOTO"));
        MetadataDTO result = fileService.getFiles(filter);
        assertNotNull(result);
    }
}
