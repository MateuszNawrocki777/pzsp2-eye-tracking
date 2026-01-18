package org.pzsp2.eye_tracking.share;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pzsp2.eye_tracking.auth.jwt.JwtService;
import org.pzsp2.eye_tracking.storage.Study;
import org.pzsp2.eye_tracking.storage.StudyMaterialRepository;
import org.pzsp2.eye_tracking.storage.StudyRepository;
import org.pzsp2.eye_tracking.session.StudySessionRepository;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.UUID;
import java.util.Objects;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class StudyShareLinkControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private StudyRepository studyRepository;

    @Autowired
    private StudyMaterialRepository materialRepository;

    @Autowired
    private StudyShareLinkRepository shareLinkRepository;

    @Autowired
    private StudySessionRepository sessionRepository;

    private UserAccount owner;

    @BeforeEach
    void setUp() {
        materialRepository.deleteAll();
        shareLinkRepository.deleteAll();
        sessionRepository.deleteAll();
        studyRepository.deleteAll();
        userAccountRepository.deleteAll();

        owner = userAccountRepository.save(new UserAccount(UUID.randomUUID(), "owner@test.local", "pw", UserRole.USER));
    }

    private String bearer(UserAccount account) {
        return "Bearer " + jwtService.generateToken(account).token();
    }

    @Test
    void createShareLink_and_access_returnsDetails() throws Exception {
        Study study = new Study();
        study.setTitle("ShareTest");
        study.setResearcherId(owner.getUserId());
        study.setSettings("{}");
        study = studyRepository.save(study);

        String payload = "{\"max_uses\":2}";

        MvcResult res = mockMvc.perform(post("/api/tests/" + study.getStudyId() + "/share")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(payload)
                .header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        String accessLink = json.get("access_link").asText();

        mockMvc.perform(get("/api/tests/share/" + accessLink))
                .andExpect(status().isOk());
    }

    @Test
    void shareLink_maxUses_exhausted_returns404() throws Exception {
        Study study = new Study();
        study.setTitle("ShareOnce");
        study.setResearcherId(owner.getUserId());
        study.setSettings("{}");
        study = studyRepository.save(study);

        String payload = "{\"max_uses\":1}";

        MvcResult res = mockMvc.perform(post("/api/tests/" + study.getStudyId() + "/share")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(payload)
                .header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        String accessLink = json.get("access_link").asText();

        mockMvc.perform(get("/api/tests/share/" + accessLink))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/tests/share/" + accessLink))
                .andExpect(status().isNotFound());
    }

    @Test
    void shareLink_expired_returns404() throws Exception {
        Study study = new Study();
        study.setTitle("ShareExpired");
        study.setResearcherId(owner.getUserId());
        study.setSettings("{}");
        study = studyRepository.save(study);

        String payload = "{\"expires_at\":\"2000-01-01T00:00:00\"}";

        MvcResult res = mockMvc.perform(post("/api/tests/" + study.getStudyId() + "/share")
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(payload)
                .header("Authorization", bearer(owner)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode json = objectMapper.readTree(res.getResponse().getContentAsString());
        String accessLink = json.get("access_link").asText();

        mockMvc.perform(get("/api/tests/share/" + accessLink))
                .andExpect(status().isNotFound());
    }
}