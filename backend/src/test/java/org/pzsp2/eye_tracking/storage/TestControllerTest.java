package org.pzsp2.eye_tracking.storage;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pzsp2.eye_tracking.auth.jwt.JwtService;
import org.pzsp2.eye_tracking.session.StudySessionRepository;
import org.pzsp2.eye_tracking.share.StudyShareLinkRepository;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class TestControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private UserAccountRepository userAccountRepository;

  @Autowired private JwtService jwtService;

  @Autowired private StudyRepository studyRepository;

  @Autowired private StudyMaterialRepository materialRepository;

  @Autowired private StudySessionRepository sessionRepository;

  @Autowired private StudyShareLinkRepository shareLinkRepository;

  private UserAccount owner;
  private UserAccount other;

  @BeforeEach
  void setUp() {
    materialRepository.deleteAll();
    sessionRepository.deleteAll();
    shareLinkRepository.deleteAll();
    studyRepository.deleteAll();
    userAccountRepository.deleteAll();

    owner =
        userAccountRepository.save(
            new UserAccount(UUID.randomUUID(), "owner@test.local", "pw", UserRole.USER));
    other =
        userAccountRepository.save(
            new UserAccount(UUID.randomUUID(), "other@test.local", "pw", UserRole.USER));
  }

  private String bearer(UserAccount account) {
    return "Bearer " + jwtService.generateToken(account).token();
  }

  @Test
  void getAllTests_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(get("/api/tests").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getAllTests_owner_seesTheirTests() throws Exception {
    Study study = new Study();
    study.setTitle("OwnerTest");
    study.setDescription("desc");
    study.setResearcherId(owner.getUserId());
    study = studyRepository.save(study);

    StudyMaterial m = new StudyMaterial();
    m.setStudy(study);
    m.setFileName("a.png");
    m.setFilePath("a.png");
    m.setDisplayOrder(1);
    m.setContentType("image/png");
    materialRepository.save(m);

    mockMvc
        .perform(get("/api/tests").header("Authorization", bearer(owner)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].title").value("OwnerTest"));
  }

  @Test
  void createTest_withFiles_createsStudyAndReturnsId() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("files", "a.txt", "text/plain", "hello".getBytes());
    MockMultipartFile settings =
        new MockMultipartFile(
            "settings",
            "settings",
            "application/json",
            objectMapper.writeValueAsBytes(
                java.util.Map.of(
                    "title", "CreatedTest",
                    "description", "desc")));

    MvcResult res =
        mockMvc
            .perform(
                multipart("/api/tests")
                    .file(file)
                    .file(settings)
                    .header("Authorization", bearer(owner)))
            .andExpect(status().isOk())
            .andReturn();

    String body = res.getResponse().getContentAsString();
    UUID createdId = UUID.fromString(body.replaceAll("\"", ""));
    UUID cid = Objects.requireNonNull(createdId);
    assertTrue(studyRepository.findById(cid).isPresent());
  }

  @Test
  void createTest_withMultipleFiles_createsStudyAndStoresAllMaterials() throws Exception {
    MockMultipartFile f1 = new MockMultipartFile("files", "a1.png", "image/png", "a".getBytes());
    MockMultipartFile f2 = new MockMultipartFile("files", "a2.png", "image/png", "b".getBytes());
    MockMultipartFile f3 = new MockMultipartFile("files", "a3.png", "image/png", "c".getBytes());
    MockMultipartFile settings =
        new MockMultipartFile(
            "settings",
            "settings",
            "application/json",
            objectMapper.writeValueAsBytes(
                java.util.Map.of(
                    "title", "MultiTest",
                    "description", "desc")));

    MvcResult res =
        mockMvc
            .perform(
                multipart("/api/tests")
                    .file(f1)
                    .file(f2)
                    .file(f3)
                    .file(settings)
                    .header("Authorization", bearer(owner)))
            .andExpect(status().isOk())
            .andReturn();

    String body = res.getResponse().getContentAsString();
    UUID createdId = UUID.fromString(body.replaceAll("\"", ""));
    UUID cid = Objects.requireNonNull(createdId);

    long materialsForStudy =
        materialRepository.findAll().stream()
            .filter(m -> Objects.equals(m.getStudy().getStudyId(), cid))
            .count();

    assertEquals(3, materialsForStudy);
  }

  @Test
  void updateTest_owner_canUpdate() throws Exception {
    Study study = new Study();
    study.setTitle("ToUpdate");
    study.setResearcherId(owner.getUserId());
    study = studyRepository.save(study);

    String payload = "{\"title\":\"Updated\",\"description\":\"new\"}";

    UUID sid = Objects.requireNonNull(study.getStudyId());
    mockMvc
        .perform(
            put("/api/tests/" + sid)
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(payload)
                .header("Authorization", bearer(owner)))
        .andExpect(status().isOk());

    assertEquals("Updated", studyRepository.findById(sid).orElseThrow().getTitle());
    assertEquals("new", studyRepository.findById(sid).orElseThrow().getDescription());
  }

  @Test
  void downloadFile_returnsFileContents() throws Exception {

    Path p = Files.createTempFile("dl-test", ".txt");
    Files.write(p, "test".getBytes());

    Study study = new Study();
    study.setTitle("DL");
    study.setResearcherId(owner.getUserId());
    study = studyRepository.save(study);

    StudyMaterial m = new StudyMaterial();
    m.setStudy(study);
    m.setFileName("dl-test.txt");
    m.setFilePath(p.toAbsolutePath().toString());
    m.setContentType("text/plain");
    m = materialRepository.save(m);

    mockMvc
        .perform(get("/api/tests/files/" + m.getMaterialId()))
        .andExpect(status().isOk())
        .andExpect(
            result -> assertTrue(result.getResponse().getContentAsString().contains("test")));
  }

  @Test
  void deleteTest_nonOwner_forbidden_owner_allowed() throws Exception {
    Study study = new Study();
    study.setTitle("ToDelete");
    study.setResearcherId(owner.getUserId());
    study = studyRepository.save(study);

    mockMvc
        .perform(delete("/api/tests/" + study.getStudyId()).header("Authorization", bearer(other)))
        .andExpect(status().isForbidden());

    mockMvc
        .perform(delete("/api/tests/" + study.getStudyId()).header("Authorization", bearer(owner)))
        .andExpect(status().isNoContent());

    UUID id = Objects.requireNonNull(study.getStudyId());
    assertTrue(studyRepository.findById(id).isEmpty());
  }

  @Test
  void createTest_unauthenticated_returns401() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("files", "test.txt", "text/plain", "content".getBytes());

    mockMvc.perform(multipart("/api/tests").file(file)).andExpect(status().isUnauthorized());
  }

  @Test
  void createTest_invalidJsonSettings_returns400() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("files", "test.txt", "text/plain", "content".getBytes());

    MockMultipartFile settings =
        new MockMultipartFile("settings", "", "application/json", "{ \"title\": ".getBytes());

    mockMvc
        .perform(
            multipart("/api/tests")
                .file(file)
                .file(settings)
                .header("Authorization", bearer(owner)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void getTestDetails_owner_returnsDetails() throws Exception {
    Study study = new Study();
    study.setTitle("DetailsTest");
    study.setResearcherId(owner.getUserId());
    study.setSettings("{\"dispGazeTracking\":true}");
    study = studyRepository.save(study);

    mockMvc
        .perform(get("/api/tests/" + study.getStudyId()).header("Authorization", bearer(owner)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.title").value("DetailsTest"))
        .andExpect(jsonPath("$.dispGazeTracking").value(true));
  }

  @Test
  void getTestDetails_unauthenticated_returns401() throws Exception {
    mockMvc.perform(get("/api/tests/" + UUID.randomUUID())).andExpect(status().isUnauthorized());
  }

  @Test
  void updateTest_unauthenticated_returns401() throws Exception {
    mockMvc
        .perform(
            put("/api/tests/" + UUID.randomUUID())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void deleteTest_unauthenticated_returns401() throws Exception {
    mockMvc.perform(delete("/api/tests/" + UUID.randomUUID())).andExpect(status().isUnauthorized());
  }

  @Test
  void createTest_jsonSettingsIsWhitespace_ignoresIt() throws Exception {
    MockMultipartFile file =
        new MockMultipartFile("files", "a.txt", "text/plain", "content".getBytes());
    MockMultipartFile settings =
        new MockMultipartFile("settings", "", "application/json", "   ".getBytes());

    mockMvc
        .perform(
            multipart("/api/tests")
                .file(file)
                .file(settings)
                .header("Authorization", bearer(owner)))
        .andExpect(status().isOk());
  }
}
