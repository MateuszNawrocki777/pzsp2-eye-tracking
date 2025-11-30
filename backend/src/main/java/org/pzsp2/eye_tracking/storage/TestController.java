package org.pzsp2.eye_tracking.storage;

import org.pzsp2.eye_tracking.storage.dto.TestCreateRequest;
import org.pzsp2.eye_tracking.storage.dto.TestDetailsDto;
import org.pzsp2.eye_tracking.storage.dto.TestListItemDto;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import java.util.UUID;

@RestController
@RequestMapping("/api/tests")
public class TestController {

    private final FileStorageService fileStorageService;
    // TODO: Stałe ID do testów
    private final UUID TEST_USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    public TestController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping(consumes = { "multipart/form-data" })
    public ResponseEntity<UUID> createTest(
            @RequestPart("files") MultipartFile[] files,
            @ModelAttribute TestCreateRequest settings) {
        UUID newTestId = fileStorageService.createFullTest(settings, files, TEST_USER_ID);
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
    public ResponseEntity<List<TestListItemDto>> getAllTests() {
        List<TestListItemDto> tests = fileStorageService.getAllTests();
        return ResponseEntity.ok(tests);
    }

    @GetMapping("/{testId}")
    public ResponseEntity<TestDetailsDto> getTestDetails(@PathVariable UUID testId) {
        TestDetailsDto details = fileStorageService.getTestDetails(testId);
        return ResponseEntity.ok(details);
    }

    @DeleteMapping("/{testId}")
    public ResponseEntity<Void> deleteTest(@PathVariable UUID testId) {
        fileStorageService.deleteTest(testId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{testId}")
    public ResponseEntity<Void> updateTest(@PathVariable UUID testId, @RequestBody TestCreateRequest settings) {
        fileStorageService.updateTestSettings(testId, settings);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{testId}/files")
    public ResponseEntity<Void> addFileToTest(
            @PathVariable UUID testId,
            @RequestParam("file") MultipartFile file) {
        fileStorageService.addFileToTest(testId, file);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/files/{fileId}")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID fileId) {
        fileStorageService.deleteSingleFile(fileId);
        return ResponseEntity.noContent().build();
    }
}