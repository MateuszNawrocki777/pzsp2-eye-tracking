package org.pzsp2.eye_tracking.storage;

import org.pzsp2.eye_tracking.storage.dto.TestCreateRequest;
import org.pzsp2.eye_tracking.storage.dto.TestDetailsDto;
import org.pzsp2.eye_tracking.storage.dto.TestListItemDto;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.pzsp2.eye_tracking.auth.AuthenticatedUser;
import org.springframework.web.multipart.MultipartFile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.web.server.ResponseStatusException;
import io.swagger.v3.oas.annotations.Parameter;
import org.springdoc.core.annotations.ParameterObject;

import java.util.List;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TestController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<UUID> createTest(
            @RequestPart("files") MultipartFile[] files,
            @ParameterObject @ModelAttribute TestCreateRequest settings,
            @Parameter(hidden = true) @RequestPart(value = "settings", required = false) String settingsJson,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TestCreateRequest finalSettings = settings;
        if (settingsJson != null && !settingsJson.isBlank()) {
            try {
                finalSettings = objectMapper.readValue(settingsJson, TestCreateRequest.class);
            } catch (JsonProcessingException e) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid settings JSON", e);
            }
        }

        UUID newTestId = fileStorageService.createFullTest(finalSettings, files, authenticatedUser.userId());
        return ResponseEntity.ok(newTestId);
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID fileId) {
        Resource resource = fileStorageService.loadFileAsResource(fileId);
        String contentType = fileStorageService.getContentType(fileId);
        String fileName = fileStorageService.getOriginalName(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(java.util.Objects.requireNonNull(contentType)))
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                .body(resource);
    }

    @GetMapping
    public ResponseEntity<List<TestListItemDto>> getAllTests(
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<TestListItemDto> tests = fileStorageService.getAllTestsForResearcher(authenticatedUser.userId());
        return ResponseEntity.ok(tests);
    }

    @GetMapping("/{testId}")
    public ResponseEntity<TestDetailsDto> getTestDetails(@PathVariable UUID testId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TestDetailsDto details = fileStorageService.getTestDetailsForResearcher(testId, authenticatedUser.userId());
        return ResponseEntity.ok(details);
    }

    @DeleteMapping("/{testId}")
    public ResponseEntity<Void> deleteTest(@PathVariable UUID testId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        fileStorageService.deleteTestForResearcher(testId, authenticatedUser.userId());
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{testId}")
    public ResponseEntity<Void> updateTest(@PathVariable UUID testId, @RequestBody TestCreateRequest settings,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        fileStorageService.updateTestSettingsForResearcher(testId, settings, authenticatedUser.userId());
        return ResponseEntity.ok().build();
    }

}