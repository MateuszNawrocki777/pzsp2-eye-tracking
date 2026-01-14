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

import java.util.List;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final FileStorageService fileStorageService;

    public TestController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<UUID> createTest(
            @RequestPart("files") MultipartFile[] files,
            @ModelAttribute TestCreateRequest settings,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {

        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID newTestId = fileStorageService.createFullTest(settings, files, authenticatedUser.userId());
        return ResponseEntity.ok(newTestId);
    }

    @GetMapping("/files/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable UUID fileId) {
        Resource resource = fileStorageService.loadFileAsResource(fileId);
        String contentType = fileStorageService.getContentType(fileId);
        String fileName = fileStorageService.getOriginalName(fileId);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
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

    @PostMapping("/{testId}/files")
    public ResponseEntity<Void> addFileToTest(
            @PathVariable UUID testId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        fileStorageService.addFileToTestForResearcher(testId, file, authenticatedUser.userId());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID fileId,
            @AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        if (authenticatedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        fileStorageService.deleteSingleFileForResearcher(fileId, authenticatedUser.userId());
        return ResponseEntity.noContent().build();
    }
}