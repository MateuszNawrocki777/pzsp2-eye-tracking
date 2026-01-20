package org.pzsp2.eye_tracking.session;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pzsp2.eye_tracking.auth.jwt.JwtService;
import org.pzsp2.eye_tracking.share.StudyShareLinkRepository;
import org.pzsp2.eye_tracking.storage.Study;
import org.pzsp2.eye_tracking.storage.StudyMaterial;
import org.pzsp2.eye_tracking.storage.StudyMaterialRepository;
import org.pzsp2.eye_tracking.storage.StudyRepository;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
class StudySessionControllerTest {

  @Autowired private MockMvc mockMvc;

  @Autowired private ObjectMapper objectMapper;

  @Autowired private StudyRepository studyRepository;

  @Autowired private StudySessionRepository sessionRepository;

  @Autowired private StudyMaterialRepository materialRepository;

  @Autowired private StudyShareLinkRepository shareLinkRepository;

  @Autowired private UserAccountRepository userAccountRepository;

  @Autowired private JwtService jwtService;

  private UserAccount owner;
  private UserAccount intruder;

  @BeforeEach
  void setUp() {
    shareLinkRepository.deleteAll();
    sessionRepository.deleteAll();
    materialRepository.deleteAll();
    studyRepository.deleteAll();
    userAccountRepository.deleteAll();

    owner =
        userAccountRepository.save(
            new UserAccount(UUID.randomUUID(), "owner@test.local", "pw", UserRole.USER));
    intruder =
        userAccountRepository.save(
            new UserAccount(UUID.randomUUID(), "intruder@test.local", "pw", UserRole.USER));
  }

  private String bearer(UserAccount account) {
    return "Bearer " + jwtService.generateToken(account).token();
  }

  @Test
  void createSession_Success_BuildsHeatmaps() throws Exception {
    Study study = createStudy(owner);
    createMaterials(study);

    Map<String, Object> payload =
        Map.of(
            "study_id", study.getStudyId(),
            "name", "session-1",
            "points_per_image",
                List.of(
                    List.of(List.of(0.5, 0.5), List.of(0.5, 0.5), List.of(0.0, 0.0)),
                    List.of(List.of(1.0, 1.0))));

    MvcResult res =
        mockMvc
            .perform(
                post("/api/sessions")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(payload)))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
    assertNotNull(json.get("session_id").asText());
  }

  @Test
  void getSession_Success_ReturnsDetailsAndMath() throws Exception {
    Study study = createStudy(owner);
    createMaterials(study);

    String jsonWithDoubleBrackets = "[[{\"x\":0,\"y\":0,\"val\":1.0}]]";

    StudySession session = createSessionInDb(study, "S1", jsonWithDoubleBrackets);

    MvcResult getRes =
        mockMvc
            .perform(
                get("/api/sessions/" + session.getSessionId())
                    .header("Authorization", bearer(owner)))
            .andExpect(status().isOk())
            .andReturn();

    JsonNode details = objectMapper.readTree(getRes.getResponse().getContentAsString());
    assertEquals(session.getSessionId().toString(), details.get("session_id").asText());
    assertEquals("S1", details.get("name").asText());

    JsonNode heatmaps = details.get("heatmaps");
    assertNotNull(heatmaps);
    assertEquals(1, heatmaps.size());
  }

  @Test
  void getSession_Unauthorized_WhenNoToken() throws Exception {
    mockMvc.perform(get("/api/sessions/" + UUID.randomUUID())).andExpect(status().isUnauthorized());
  }

  @Test
  void getSession_NotFound_WhenSessionDoesNotExist() throws Exception {
    mockMvc
        .perform(get("/api/sessions/" + UUID.randomUUID()).header("Authorization", bearer(owner)))
        .andExpect(status().isNotFound());
  }

  @Test
  void getSession_Forbidden_WhenUserIsNotResearcher() throws Exception {
    Study study = createStudy(owner);
    StudySession session = createSessionInDb(study, "Secret", "[]");

    mockMvc
        .perform(
            get("/api/sessions/" + session.getSessionId())
                .header("Authorization", bearer(intruder)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getSessionsForStudy_Success_ReturnsList() throws Exception {
    Study study = createStudy(owner);
    createSessionInDb(study, "S1", "[]");
    createSessionInDb(study, "S2", "[]");

    mockMvc
        .perform(
            get("/api/sessions/test/" + study.getStudyId()).header("Authorization", bearer(owner)))
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              String json = result.getResponse().getContentAsString();
              JsonNode array = objectMapper.readTree(json);
              assertEquals(2, array.size());
            });
  }

  @Test
  void getSessionsForStudy_Unauthorized_WhenNoToken() throws Exception {
    mockMvc
        .perform(get("/api/sessions/test/" + UUID.randomUUID()))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getSessionsForStudy_NotFound_WhenStudyDoesNotExist() throws Exception {
    mockMvc
        .perform(
            get("/api/sessions/test/" + UUID.randomUUID()).header("Authorization", bearer(owner)))
        .andExpect(status().isNotFound());
  }

  @Test
  void getSessionsForStudy_Forbidden_WhenIntruder() throws Exception {
    Study study = createStudy(owner);
    mockMvc
        .perform(
            get("/api/sessions/test/" + study.getStudyId())
                .header("Authorization", bearer(intruder)))
        .andExpect(status().isForbidden());
  }

  @Test
  void getAggregateHeatmap_Success() throws Exception {
    Study study = createStudy(owner);
    createMaterials(study);

    study.setAggregateHeatmapsJson("[]");
    studyRepository.save(study);

    mockMvc
        .perform(
            get("/api/sessions/test/" + study.getStudyId() + "/heatmap")
                .header("Authorization", bearer(owner)))
        .andExpect(status().isOk())
        .andExpect(
            result -> {
              String json = result.getResponse().getContentAsString();
              assertNotNull(objectMapper.readTree(json).get("heatmaps"));
            });
  }

  @Test
  void getAggregateHeatmap_Unauthorized() throws Exception {
    mockMvc
        .perform(get("/api/sessions/test/" + UUID.randomUUID() + "/heatmap"))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void getAggregateHeatmap_NotFound() throws Exception {
    mockMvc
        .perform(
            get("/api/sessions/test/" + UUID.randomUUID() + "/heatmap")
                .header("Authorization", bearer(owner)))
        .andExpect(status().isNotFound());
  }

  @Test
  void getAggregateHeatmap_Forbidden() throws Exception {
    Study study = createStudy(owner);
    mockMvc
        .perform(
            get("/api/sessions/test/" + study.getStudyId() + "/heatmap")
                .header("Authorization", bearer(intruder)))
        .andExpect(status().isForbidden());
  }

  private Study createStudy(UserAccount user) {
    Study study = new Study();
    study.setTitle("Test Study");
    study.setSettings("{}");
    study.setResearcherId(user.getUserId());
    return studyRepository.save(study);
  }

  private void createMaterials(Study study) {
    StudyMaterial m1 = new StudyMaterial();
    m1.setStudy(study);
    m1.setFileName("a.png");
    m1.setFilePath("a.png");
    m1.setDisplayOrder(1);
    m1.setContentType("image/png");

    StudyMaterial m2 = new StudyMaterial();
    m2.setStudy(study);
    m2.setFileName("b.png");
    m2.setFilePath("b.png");
    m2.setDisplayOrder(2);
    m2.setContentType("image/png");

    materialRepository.saveAll(List.of(m1, m2));
  }

  private StudySession createSessionInDb(Study study, String name, String json) {
    StudySession s = new StudySession();
    s.setStudy(study);
    s.setName(name);
    s.setHeatmapsJson(json);
    return sessionRepository.save(s);
  }
}
