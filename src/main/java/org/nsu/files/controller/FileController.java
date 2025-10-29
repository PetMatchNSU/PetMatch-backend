package org.nsu.files.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.nsu.authorization.core.security.PersonDetails;
import org.nsu.files.dto.*;
import org.nsu.files.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "File Management", description = "API for managing files in the PetMatch system")
public class FileController {

    private final FileService fileService;
    private final ObjectMapper objectMapper;

    @Autowired
    public FileController(FileService fileService, ObjectMapper objectMapper) {
        this.fileService = fileService;
        this.objectMapper = objectMapper;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload files", description = "Upload multiple files with metadata for an animal card")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Files uploaded successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetadataDTO.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request data or file count mismatch"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<MetadataDTO> uploadFiles(
            @Parameter(description = "Array of files to upload") @RequestPart("files") MultipartFile[] files,
            @Parameter(description = "JSON metadata for files") @RequestPart("metadata") String metadataJson,
            @Parameter(description = "Animal card ID") @RequestPart("adId") Long adId) {
        try {
            MetadataDTO metadata = objectMapper.readValue(metadataJson, MetadataDTO.class);
            if (files.length != metadata.descriptors().size()) {
                return ResponseEntity.badRequest().build();
            }
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            PersonDetails personDetails = (PersonDetails) authentication.getPrincipal();
            Long userId = personDetails.getUserId();
            List<FileDescriptor> descriptors = fileService.validateAndPublishUpload(List.of(files), metadata, userId, adId);
            return ResponseEntity.ok(new MetadataDTO(descriptors));
        } catch (JsonProcessingException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping
    @Operation(summary = "Get files", description = "Retrieve files based on filter criteria")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Files retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetadataDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<MetadataDTO> getFiles(@Parameter(description = "Base64 encoded filter query") @RequestParam("query") String query) {
        try {
            String decodedQuery = new String(Base64.getDecoder().decode(query));
            FilterDTO filter = objectMapper.readValue(decodedQuery, FilterDTO.class);
            MetadataDTO result = fileService.getFiles(filter);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping
    @Operation(summary = "Delete files", description = "Delete files by IDs or animal card IDs")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Files deleted successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = MetadataDTO.class))),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<MetadataDTO> deleteFiles(@Parameter(description = "Delete request with file and card IDs") @RequestBody DeleteRequest deleteRequest) {
        try {
            MetadataDTO result = fileService.deleteFiles(deleteRequest);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
