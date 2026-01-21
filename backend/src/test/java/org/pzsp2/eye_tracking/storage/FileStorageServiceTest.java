package org.pzsp2.eye_tracking.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.HttpStatus.NOT_FOUND;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pzsp2.eye_tracking.share.StudyShareLinkService;
import org.pzsp2.eye_tracking.storage.dto.TestCreateRequest;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class) class FileStorageServiceTest {

    @Mock private StudyRepository studyRepository;

    @Mock private StudyMaterialRepository materialRepository;

    @Mock private StudyShareLinkService shareLinkService;

    private FileStorageService service;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @TempDir Path tempDir;

    @BeforeEach void setUp() {
        service = new FileStorageService(tempDir.toString(), materialRepository, studyRepository,
                        shareLinkService);

        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test void updateTestSettings_allowsOwner_andMergesFields() throws JsonProcessingException {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();

        Study study = new Study();
        study.setStudyId(testId);
        study.setResearcherId(owner);
        TestCreateRequest oldSettings = new TestCreateRequest();
        oldSettings.setDispGazeTracking(false);
        oldSettings.setTimePerImageMs(1000);
        study.setSettings(objectMapper.writeValueAsString(oldSettings));

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        TestCreateRequest req = new TestCreateRequest();
        req.setTitle("New Title");
        req.setDispGazeTracking(true);

        service.updateTestSettingsForResearcher(testId, req, owner);

        verify(studyRepository).save(study);
        assertEquals("New Title", study.getTitle());

        TestCreateRequest merged = objectMapper.readValue(study.getSettings(),
                        TestCreateRequest.class);
        assertTrue(merged.getDispGazeTracking());
        assertEquals(1000, merged.getTimePerImageMs());
    }

    @Test void updateTestSettings_deniesNonOwner() {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        Study study = new Study();
        study.setResearcherId(owner);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> service
                        .updateTestSettingsForResearcher(testId, new TestCreateRequest(), other));
        assertEquals(FORBIDDEN, ex.getStatusCode());
    }

    @Test void updateTestSettings_throwsNotFound_whenStudyMissing() {
        UUID testId = UUID.randomUUID();
        given(studyRepository.findById(testId)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                        () -> service.updateTestSettingsForResearcher(testId,
                                        new TestCreateRequest(), UUID.randomUUID()));
        assertEquals(NOT_FOUND, ex.getStatusCode());
    }

    @Test void updateTestSettings_handlesCorruptedJsonInDb() {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        Study study = new Study();
        study.setResearcherId(owner);
        study.setSettings("{ corrupted json ...");

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        assertThrows(RuntimeException.class, () -> service.updateTestSettingsForResearcher(testId,
                        new TestCreateRequest(), owner));
    }

    @Test void deleteTest_allowsOwner_andDeletesFiles() throws IOException {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();

        Study study = new Study();
        study.setResearcherId(owner);

        Path file = tempDir.resolve("delete_me.txt");
        Files.createFile(file);

        StudyMaterial m1 = new StudyMaterial();
        m1.setFilePath("delete_me.txt");
        m1.setStudy(study);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));
        given(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .willReturn(List.of(m1));

        service.deleteTestForResearcher(testId, owner);

        assertFalse(Files.exists(file), "File should be deleted");
        verify(materialRepository).deleteAll(any());
        verify(studyRepository).delete(study);
    }

    @Test void deleteTest_throwsNotFound() {
        UUID testId = UUID.randomUUID();
        given(studyRepository.findById(testId)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                        () -> service.deleteTestForResearcher(testId, UUID.randomUUID()));
        assertEquals(NOT_FOUND, ex.getStatusCode());
    }

    @Test void deleteTest_deniesNonOwner() {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        Study study = new Study();
        study.setResearcherId(owner);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                        () -> service.deleteTestForResearcher(testId, UUID.randomUUID()));
        assertEquals(FORBIDDEN, ex.getStatusCode());
    }

    @Test void getTestDetails_allowsOwner_andMapsJson() throws Exception {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();

        TestCreateRequest req = new TestCreateRequest();
        req.setDispGazeTracking(true);
        req.setTimePerImageMs(500);

        Study study = new Study();
        study.setStudyId(testId);
        study.setResearcherId(owner);
        study.setTitle("Test");
        study.setSettings(objectMapper.writeValueAsString(req));

        StudyMaterial m1 = new StudyMaterial();
        m1.setMaterialId(UUID.randomUUID());
        m1.setStudy(study);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));
        given(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .willReturn(List.of(m1));

        var dto = service.getTestDetailsForResearcher(testId, owner);

        assertEquals("Test", dto.getTitle());
        assertEquals(Boolean.TRUE, dto.getDispGazeTracking());
        assertEquals(500, dto.getTimePerImageMs());
        assertEquals(1, dto.getFileLinks().size());
    }

    @Test void getTestDetails_throwsNotFound() {
        UUID testId = UUID.randomUUID();
        given(studyRepository.findById(testId)).willReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                        () -> service.getTestDetailsForResearcher(testId, UUID.randomUUID()));
        assertEquals(NOT_FOUND, ex.getStatusCode());
    }

    @Test void getTestDetails_throwsForbidden() {
        UUID testId = UUID.randomUUID();
        Study study = new Study();
        study.setResearcherId(UUID.randomUUID());

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                        () -> service.getTestDetailsForResearcher(testId, UUID.randomUUID()));
        assertEquals(FORBIDDEN, ex.getStatusCode());
    }

    @Test void getTestDetails_handlesJsonError() {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        Study study = new Study();
        study.setResearcherId(owner);
        study.setSettings("{ bad json");

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        assertThrows(RuntimeException.class,
                        () -> service.getTestDetailsForResearcher(testId, owner));
    }

    @Test void createFullTest_savesStudyAndFiles() throws IOException {
        UUID owner = UUID.randomUUID();
        TestCreateRequest req = new TestCreateRequest();
        req.setTitle("Title");

        MockMultipartFile f1 = new MockMultipartFile("files", "a.png", "image/png",
                        "data".getBytes());

        given(studyRepository.save(any(Study.class))).willAnswer(inv -> {
            Study s = inv.getArgument(0);
            s.setStudyId(UUID.randomUUID());
            return s;
        });

        UUID createdId = service.createFullTest(req, new MultipartFile[]{f1}, owner);

        assertNotNull(createdId);
        verify(materialRepository).save(any(StudyMaterial.class));
        assertTrue(Files.list(tempDir).findAny().isPresent());
    }

    @Test void createFullTest_handlesIOException_fromMultipart() {
        UUID owner = UUID.randomUUID();
        TestCreateRequest req = new TestCreateRequest();

        MultipartFile badFile = mock(MultipartFile.class);
        try {
            when(badFile.getOriginalFilename()).thenReturn("bad.png");
            when(badFile.getInputStream()).thenThrow(new IOException("Simulated IO Error"));
        } catch (IOException e) {
        }

        given(studyRepository.save(any(Study.class))).willReturn(new Study());

        assertThrows(RuntimeException.class,
                        () -> service.createFullTest(req, new MultipartFile[]{badFile}, owner));
    }

    @Test void getAllTests_returnsMappedList() {
        UUID owner = UUID.randomUUID();
        Study study = new Study();
        study.setStudyId(UUID.randomUUID());
        study.setTitle("My Test");
        study.setResearcherId(owner);

        StudyMaterial m = new StudyMaterial();
        m.setMaterialId(UUID.randomUUID());

        given(studyRepository.findAll()).willReturn(List.of(study));
        given(materialRepository.findFirstByStudyOrderByDisplayOrderAsc(study))
                        .willReturn(Optional.of(m));

        var list = service.getAllTestsForResearcher(owner);

        assertEquals(1, list.size());
        assertNotNull(list.get(0).getFirstImageLink());
    }

    @Test void getAllTests_handlesMissingImage() {
        UUID owner = UUID.randomUUID();
        Study study = new Study();
        study.setResearcherId(owner);

        given(studyRepository.findAll()).willReturn(List.of(study));
        given(materialRepository.findFirstByStudyOrderByDisplayOrderAsc(study))
                        .willReturn(Optional.empty());

        var list = service.getAllTestsForResearcher(owner);

        assertEquals(1, list.size());
        assertNull(list.get(0).getFirstImageLink());
    }

    @Test void loadFileAsResource_success() throws IOException {
        UUID fileId = UUID.randomUUID();
        String filename = "test.txt";

        Files.createFile(tempDir.resolve(filename));

        StudyMaterial mat = new StudyMaterial();
        mat.setFilePath(filename);

        given(materialRepository.findById(fileId)).willReturn(Optional.of(mat));

        Resource res = service.loadFileAsResource(fileId);
        assertTrue(res.exists());
    }

    @Test void loadFileAsResource_throwsIfMaterialNotFound() {
        UUID fileId = UUID.randomUUID();
        given(materialRepository.findById(fileId)).willReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.loadFileAsResource(fileId));
    }

    @Test void loadFileAsResource_throwsIfFileMissingOnDisk() {
        UUID fileId = UUID.randomUUID();
        StudyMaterial mat = new StudyMaterial();
        mat.setFilePath("ghost_file.txt");

        given(materialRepository.findById(fileId)).willReturn(Optional.of(mat));

        RuntimeException ex = assertThrows(RuntimeException.class,
                        () -> service.loadFileAsResource(fileId));
        assertEquals("File not found", ex.getMessage());
    }

    @Test void getContentType_returnsStoredType() {
        UUID fileId = UUID.randomUUID();
        StudyMaterial mat = new StudyMaterial();
        mat.setContentType("image/png");

        given(materialRepository.findById(fileId)).willReturn(Optional.of(mat));

        assertEquals("image/png", service.getContentType(fileId));
    }

    @Test void getContentType_returnsDefault_whenNotFound() {
        UUID fileId = UUID.randomUUID();
        given(materialRepository.findById(fileId)).willReturn(Optional.empty());

        assertEquals("application/octet-stream", service.getContentType(fileId));
    }

    @Test void getOriginalName_returnsName() {
        UUID fileId = UUID.randomUUID();
        StudyMaterial mat = new StudyMaterial();
        mat.setFileName("my_photo.jpg");

        given(materialRepository.findById(fileId)).willReturn(Optional.of(mat));

        assertEquals("my_photo.jpg", service.getOriginalName(fileId));
    }

    @Test void getOriginalName_returnsDefault() {
        UUID fileId = UUID.randomUUID();
        given(materialRepository.findById(fileId)).willReturn(Optional.empty());

        assertEquals("file", service.getOriginalName(fileId));
    }

    @Test void createFullTest_handlesFileWithoutExtension() throws IOException {
        UUID owner = UUID.randomUUID();
        TestCreateRequest req = new TestCreateRequest();
        req.setTitle("No Ext Test");

        MockMultipartFile file = new MockMultipartFile("files", "README", "text/plain",
                        "abc".getBytes());

        given(studyRepository.save(any(Study.class))).willAnswer(inv -> {
            Study s = inv.getArgument(0);
            s.setStudyId(UUID.randomUUID());
            return s;
        });

        service.createFullTest(req, new MultipartFile[]{file}, owner);

        ArgumentCaptor<StudyMaterial> captor = ArgumentCaptor.forClass(StudyMaterial.class);
        verify(materialRepository).save(captor.capture());

        String storedPath = captor.getValue().getFilePath();
        assertFalse(storedPath.contains("."),
                        "If the original file does not contain dot in its filename, saved file's name shouldn't"
                                        + " contain it too");
    }

    @Test void updateTestSettings_ignoresNullFields_andPreservesOldValues()
                    throws JsonProcessingException {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        Study study = new Study();
        study.setResearcherId(owner);

        TestCreateRequest oldSettings = new TestCreateRequest();
        oldSettings.setTitle("Old Title");
        oldSettings.setDispGazeTracking(true);
        study.setSettings(objectMapper.writeValueAsString(oldSettings));
        study.setTitle("Old Title");

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        TestCreateRequest newSettings = new TestCreateRequest();

        service.updateTestSettingsForResearcher(testId, newSettings, owner);

        ArgumentCaptor<Study> captor = ArgumentCaptor.forClass(Study.class);
        verify(studyRepository).save(captor.capture());

        Study saved = captor.getValue();
        assertEquals("Old Title", saved.getTitle());

        TestCreateRequest merged = objectMapper.readValue(saved.getSettings(),
                        TestCreateRequest.class);
        assertEquals("Old Title", merged.getTitle());
        assertTrue(merged.getDispGazeTracking());
    }

    @Test void updateTestSettings_handlesNullSettingsInDb() {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        Study study = new Study();
        study.setResearcherId(owner);
        study.setSettings(null);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        TestCreateRequest newSettings = new TestCreateRequest();
        newSettings.setTitle("New");

        assertDoesNotThrow(
                        () -> service.updateTestSettingsForResearcher(testId, newSettings, owner));

        verify(studyRepository).save(study);
        assertEquals("New", study.getTitle());
    }

    @Test void createFullTest_handlesFileSaveError_throwsRuntimeException() throws IOException {
        UUID owner = UUID.randomUUID();
        TestCreateRequest req = new TestCreateRequest();
        req.setTitle("Error Test");

        MultipartFile badFile = mock(MultipartFile.class);
        when(badFile.getOriginalFilename()).thenReturn("virus.exe");
        when(badFile.getInputStream()).thenThrow(new IOException("Disk Failure"));

        given(studyRepository.save(any(Study.class))).willReturn(new Study());

        RuntimeException ex = assertThrows(RuntimeException.class,
                        () -> service.createFullTest(req, new MultipartFile[]{badFile}, owner));

        assertTrue(ex.getMessage().contains("Couldn't save file"));
    }

    @Test void deleteTestForResearcher_handlesFileDeleteError_logsButContinues()
                    throws IOException {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        Study study = new Study();
        study.setResearcherId(owner);

        String dirName = "im_a_directory";
        Path dirPath = tempDir.resolve(dirName);
        Files.createDirectory(dirPath);
        Files.createFile(dirPath.resolve("lock.txt"));

        StudyMaterial m1 = new StudyMaterial();
        m1.setFilePath(dirName);
        m1.setStudy(study);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));
        given(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study))
                        .willReturn(List.of(m1));

        assertDoesNotThrow(() -> service.deleteTestForResearcher(testId, owner));

        verify(studyRepository).delete(study);
    }

    @Test void updateTestSettings_handlesBlankSettingsString() {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        Study study = new Study();
        study.setResearcherId(owner);
        study.setSettings("");

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        TestCreateRequest newSettings = new TestCreateRequest();
        newSettings.setTitle("Updated");

        service.updateTestSettingsForResearcher(testId, newSettings, owner);

        verify(studyRepository).save(study);
        assertEquals("Updated", study.getTitle());
    }

    @Test void createFullTest_handlesFileStartingWithDot() throws IOException {
        UUID owner = UUID.randomUUID();
        TestCreateRequest req = new TestCreateRequest();
        req.setTitle("Dotfile Test");

        MockMultipartFile file = new MockMultipartFile("files", ".hidden", "text/plain",
                        "abc".getBytes());

        given(studyRepository.save(any(Study.class))).willAnswer(inv -> {
            Study s = inv.getArgument(0);
            s.setStudyId(UUID.randomUUID());
            return s;
        });

        service.createFullTest(req, new MultipartFile[]{file}, owner);

        ArgumentCaptor<StudyMaterial> captor = ArgumentCaptor.forClass(StudyMaterial.class);
        verify(materialRepository).save(captor.capture());

        String storedPath = captor.getValue().getFilePath();
        assertFalse(storedPath.contains("."),
                        "File .hidden shouldn't contain dot in it's filename after saving on disk");
    }

    @Test void createFullTest_handlesEmptyFileList() {
        UUID owner = UUID.randomUUID();
        TestCreateRequest req = new TestCreateRequest();
        req.setTitle("No Files Test");

        given(studyRepository.save(any(Study.class))).willReturn(new Study());

        service.createFullTest(req, new MultipartFile[]{}, owner);

        verify(studyRepository).save(any(Study.class));
        verify(materialRepository, never()).save(any(StudyMaterial.class));
    }
}
