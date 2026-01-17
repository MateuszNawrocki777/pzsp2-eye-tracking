package org.pzsp2.eye_tracking.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.pzsp2.eye_tracking.storage.dto.TestCreateRequest;
import org.pzsp2.eye_tracking.share.StudyShareLinkService;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileStorageServiceTest {

    @Mock
    private StudyRepository studyRepository;

    @Mock
    private StudyMaterialRepository materialRepository;

    @Mock
    private StudyShareLinkService shareLinkService;

    private FileStorageService service;

    private Path tempDir;

    @BeforeEach
    void setUp() throws Exception {
        tempDir = Files.createTempDirectory("test-uploads");
        service = new FileStorageService(tempDir.toString(), materialRepository, studyRepository, shareLinkService);
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    void updateTestSettingsForResearcher_allowsOwner() {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();

        Study study = new Study();
        study.setStudyId(testId);
        study.setResearcherId(owner);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        TestCreateRequest req = new TestCreateRequest();
        req.setTitle("Title");
        req.setDescription("Description");

        service.updateTestSettingsForResearcher(testId, req, owner);

        verify(studyRepository).save(study);
        assertEquals("Title", study.getTitle());
        assertEquals("Description", study.getDescription());
    }

    @Test
    void updateTestSettingsForResearcher_deniesNonOwner() {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        Study study = new Study();
        study.setStudyId(testId);
        study.setResearcherId(owner);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        TestCreateRequest req = new TestCreateRequest();

        assertThrows(ResponseStatusException.class,
                () -> service.updateTestSettingsForResearcher(testId, req, other));
    }

    @Test
    void deleteTestForResearcher_allowsOwner_andDeletesMaterials() {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();

        Study study = new Study();
        study.setStudyId(testId);
        study.setResearcherId(owner);

        StudyMaterial m1 = new StudyMaterial();
        m1.setMaterialId(UUID.randomUUID());
        m1.setFilePath("nonexistent-file-1");
        m1.setStudy(study);

        List<StudyMaterial> materials = List.of(m1);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));
        given(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study)).willReturn(materials);

        service.deleteTestForResearcher(testId, owner);

        verify(materialRepository).deleteAll(materials);
        verify(shareLinkService).deleteLinksForStudy(study);
        verify(studyRepository).delete(study);
    }

    @Test
    void deleteTestForResearcher_deniesNonOwner() {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        Study study = new Study();
        study.setStudyId(testId);
        study.setResearcherId(owner);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        assertThrows(ResponseStatusException.class, () -> service.deleteTestForResearcher(testId, other));
    }

    @Test
    void getTestDetailsForResearcher_deniesNonOwner() throws Exception {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        UUID other = UUID.randomUUID();

        Study study = new Study();
        study.setStudyId(testId);
        study.setResearcherId(owner);
        study.setSettings(new ObjectMapper().writeValueAsString(new TestCreateRequest()));

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));

        assertThrows(ResponseStatusException.class, () -> service.getTestDetailsForResearcher(testId, other));
    }

    @Test
    void getTestDetailsForResearcher_allowsOwner() throws Exception {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();

        TestCreateRequest req = new TestCreateRequest();
        req.setDispGazeTracking(true);
        req.setDispTimeLeft(true);
        req.setTimePerImageMs(500);
        req.setRandomizeOrder(false);

        Study study = new Study();
        study.setStudyId(testId);
        study.setResearcherId(owner);
        study.setTitle("Test");
        study.setDescription("Description");
        study.setSettings(new ObjectMapper().writeValueAsString(req));

        StudyMaterial m1 = new StudyMaterial();
        m1.setMaterialId(UUID.randomUUID());
        m1.setStudy(study);

        given(studyRepository.findById(testId)).willReturn(Optional.of(study));
        given(materialRepository.findAllByStudyOrderByDisplayOrderAsc(study)).willReturn(List.of(m1));

        var dto = service.getTestDetailsForResearcher(testId, owner);

        assertEquals(testId, dto.getId());
        assertEquals("Test", dto.getTitle());
        assertEquals("Description", dto.getDescription());
        assertEquals(Boolean.TRUE, dto.getDispGazeTracking());
        assertEquals(Boolean.TRUE, dto.getDispTimeLeft());
        assertEquals(Integer.valueOf(500), dto.getTimePerImageMs());
        assertEquals(Boolean.FALSE, dto.getRandomizeOrder());
        assertNotNull(dto.getFileLinks());
        assertEquals(1, dto.getFileLinks().size());
    }

    @Test
    void createFullTest_savesStudyAndFiles() throws Exception {
        UUID owner = UUID.randomUUID();

        TestCreateRequest req = new TestCreateRequest();
        req.setTitle("Title");
        req.setDescription("Desc");

        MockMultipartFile f1 = new MockMultipartFile("files", "a.png", "image/png", "data".getBytes());
        MockMultipartFile f2 = new MockMultipartFile("files", "b.png", "image/png", "data".getBytes());

        given(studyRepository.save(any(Study.class))).willAnswer(inv -> {
            Study s = inv.getArgument(0);
            s.setStudyId(UUID.randomUUID());
            return s;
        });

        given(materialRepository.save(any(StudyMaterial.class))).willAnswer(inv -> {
            StudyMaterial m = inv.getArgument(0);
            m.setMaterialId(UUID.randomUUID());
            return m;
        });

        UUID created = service.createFullTest(req, new MultipartFile[] { f1, f2 }, owner);

        assertNotNull(created);
        verify(studyRepository).save(any(Study.class));
        verify(materialRepository, times(2)).save(any(StudyMaterial.class));

        long fileCount = Files.list(tempDir).count();
        assertTrue(fileCount > 0);
    }

    @Test
    void getAllTests_returnsMappedList() {
        UUID testId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();

        Study study = new Study();
        study.setStudyId(testId);
        study.setTitle("My Test");
        study.setResearcherId(owner);

        StudyMaterial m = new StudyMaterial();
        m.setMaterialId(UUID.randomUUID());

        given(studyRepository.findAll()).willReturn(List.of(study));
        given(materialRepository.findFirstByStudyOrderByDisplayOrderAsc(study)).willReturn(Optional.of(m));

        var list = service.getAllTestsForResearcher(owner);

        assertEquals(1, list.size());
        assertEquals(testId, list.get(0).getId());
        assertEquals("My Test", list.get(0).getTitle());
        assertNotNull(list.get(0).getFirstImageLink());
    }
}
