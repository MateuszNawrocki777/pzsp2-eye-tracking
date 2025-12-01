package org.pzsp2.eye_tracking.admin;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.pzsp2.eye_tracking.admin.dto.UpdateUserBannedRequest;
import org.pzsp2.eye_tracking.admin.dto.UpdateUserRoleRequest;
import org.pzsp2.eye_tracking.auth.jwt.JwtService;
import org.pzsp2.eye_tracking.user.UserAccount;
import org.pzsp2.eye_tracking.user.UserAccountRepository;
import org.pzsp2.eye_tracking.user.UserRole;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Objects;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminUserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserAccountRepository userAccountRepository;

    @Autowired
    private JwtService jwtService;

    private UserAccount adminAccount;
    private UserAccount userAccount;

    @BeforeEach
    void setUp() {
        userAccountRepository.deleteAll();

        adminAccount = userAccountRepository.save(new UserAccount(
                UUID.randomUUID(),
                "admin@test.local",
                "hashed-password",
                UserRole.ADMIN));

        userAccount = userAccountRepository.save(new UserAccount(
                UUID.randomUUID(),
                "user@test.local",
                "hashed-password",
                UserRole.USER));
    }

    @Test
    void listUsers_asAdmin_returnsUsers() throws Exception {
        mockMvc.perform(get("/admin/users")
                .header("Authorization", bearer(adminAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].email").value(userAccount.getEmail()));
    }

    @Test
    void listUsers_asNonAdmin_isForbidden() throws Exception {
        mockMvc.perform(get("/admin/users")
                .header("Authorization", bearer(userAccount)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateRole_promotesUser() throws Exception {
        mockMvc.perform(post("/admin/users/{userId}/role", Objects.requireNonNull(userAccount.getUserId()))
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(
                        objectMapper.writeValueAsString(new UpdateUserRoleRequest(UserRole.ADMIN))))
                .header("Authorization", bearer(adminAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        UserAccount reloaded = userAccountRepository.findById(Objects.requireNonNull(userAccount.getUserId()))
                .orElseThrow();
        assertThat(reloaded.getRole()).isEqualTo(UserRole.ADMIN);
    }

    @Test
    void updateBanned_blocksUser() throws Exception {
        mockMvc.perform(post("/admin/users/{userId}/banned", Objects.requireNonNull(userAccount.getUserId()))
                .contentType(Objects.requireNonNull(MediaType.APPLICATION_JSON))
                .content(Objects.requireNonNull(
                        objectMapper.writeValueAsString(new UpdateUserBannedRequest(true))))
                .header("Authorization", bearer(adminAccount)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.banned").value(true));

        UserAccount reloaded = userAccountRepository.findById(Objects.requireNonNull(userAccount.getUserId()))
                .orElseThrow();
        assertThat(reloaded.isBanned()).isTrue();
    }

    private String bearer(UserAccount account) {
        return "Bearer " + jwtService.generateToken(account).token();
    }
}
