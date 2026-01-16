package org.pzsp2.eye_tracking.session;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pzsp2.eye_tracking.auth.jwt.JwtService;
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

import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("null")
@SpringBootTest
@AutoConfigureMockMvc
class StudySessionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private StudySessionRepository sessionRepository;

    @Autowired
    private StudyMaterialRepository materialRepository;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private JwtService jwtService;

    private UserAccount owner;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        materialRepository.deleteAll();
        studyRepository.deleteAll();
        userAccountRepository.deleteAll();

        owner = userAccountRepository.save(new UserAccount(java.util.UUID.randomUUID(),
                "owner@test.local", "pw", UserRole.USER));
    }

    private String bearer(UserAccount account) {
        return "Bearer " + jwtService.generateToken(account).token();
    }

    @Test
    void createSession_buildsHeatmapsPerImage() throws Exception {
        Study study = new Study();
        study.setTitle("HeatmapStudy");
        study.setSettings("{}");
        study.setResearcherId(owner.getUserId());
        study = studyRepository.save(study);

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

        Map<String, Object> payload = Map.of(
                "study_id", study.getStudyId(),
                "name", "session-1",
                "points_per_image", List.of(
                        List.of(
                                List.of(0.5, 0.5),
                                List.of(0.5, 0.5),
                                List.of(0.0, 0.0)),
                        List.of(
                                List.of(1.0, 1.0))));

        MvcResult res = mockMvc.perform(post("/api/sessions")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(objectMapper.writeValueAsString(payload))))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        String sessionId = json.get("session_id").asText();
        assertNotNull(sessionId);

        MvcResult getRes = mockMvc.perform(get("/api/sessions/" + sessionId)
                .header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode details = objectMapper.readTree(getRes.getResponse().getContentAsString());
        JsonNode heatmaps = details.get("heatmaps");
        assertEquals(2, heatmaps.size());

        JsonNode image1 = heatmaps.get(0);
        double sum = 0.0;
        for (JsonNode p : image1) {
            sum += p.get("val").asDouble();
        }
        // total normalized sum should be 1.0
        assertEquals(1.0, sum, 1e-9);
    }
}