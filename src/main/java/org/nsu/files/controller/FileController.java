package org.nsu.files.controller;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.nsu.files.dto.*;
import org.nsu.files.service.FileService;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;

    @Operation(summary = "Upload files", description = "Upload files with metadata and ad ID")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<MetadataDTO> uploadFiles(
            @RequestPart("files") MultipartFile[] files,
            @RequestPart("metadata") String metadataJson,
            @RequestPart("adId") Long adId) throws JsonProcessingException {
        MetadataDTO result = fileService.uploadFiles(files, metadataJson, adId);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get files", description = "Get files based on query filter")
    @GetMapping
    public ResponseEntity<MetadataDTO> getFiles(@RequestParam("query") String query) {
        MetadataDTO result = fileService.getFiles(query);
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Delete files", description = "Delete files by IDs or animal card IDs")
    @DeleteMapping
    public ResponseEntity<MetadataDTO> deleteFiles(@RequestBody DeleteRequest deleteRequest) {
        MetadataDTO result = fileService.deleteFiles(deleteRequest);
        return ResponseEntity.ok(result);
    }
}
