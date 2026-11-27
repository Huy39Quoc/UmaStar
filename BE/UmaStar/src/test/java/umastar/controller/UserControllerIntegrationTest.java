package umastar.controller;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import umastar.entity.RefreshToken;
import umastar.entity.User;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.exception.ErrorCode;
import umastar.repository.RefreshTokenRepository;
import umastar.repository.UserRepository;
import umastar.util.TokenHasher;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class UserControllerIntegrationTest {

    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\"accessToken\":\"([^\"]+)\"");
    private static final Pattern REFRESH_TOKEN_PATTERN = Pattern.compile("\"refreshToken\":\"([^\"]+)\"");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EntityManager entityManager;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void profileEndpointsGetAndUpdateOwnProfileIgnoringImmutableFields() throws Exception {
        mockMvc.perform(get("/api/v1/profile"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));

        User staff = createStaffInDb("profile.driver@umastar.local", "Tài Xế Một", Role.DRIVER);
        String token = extractToken(loginJson("profile.driver@umastar.local", "Password1"), ACCESS_TOKEN_PATTERN);

        mockMvc.perform(get("/api/v1/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(staff.getId().toString()))
                .andExpect(jsonPath("$.email").value("profile.driver@umastar.local"))
                .andExpect(jsonPath("$.role").value("DRIVER"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        mockMvc.perform(put("/api/v1/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "  Tài Xế Đã Đổi Tên ",
                                  "phone": "+84912345678",
                                  "email": "hack@umastar.local",
                                  "role": "LOGISTICS_MANAGER"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Tài Xế Đã Đổi Tên"))
                .andExpect(jsonPath("$.phone").value("+84912345678"))
                .andExpect(jsonPath("$.email").value("profile.driver@umastar.local"))
                .andExpect(jsonPath("$.role").value("DRIVER"));

        mockMvc.perform(put("/api/v1/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "fullName": "Tên Hợp Lệ", "phone": "bad-phone" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()))
                .andExpect(jsonPath("$.errors.phone").isNotEmpty());
    }

    @Test
    void usersEndpointsRequireManagerRole() throws Exception {
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized());

        createStaffInDb("non.mgr@umastar.local", "Điều Phối", Role.FLEET_COORDINATOR);
        String token = extractToken(loginJson("non.mgr@umastar.local", "Password1"), ACCESS_TOKEN_PATTERN);

        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()));
    }

    @Test
    void managerCanCreateFilterListAndUpdateUserStatus() throws Exception {
        User manager = createStaffInDb("mgr.test@umastar.local", "Quản Lý Test", Role.LOGISTICS_MANAGER);
        String managerToken = extractToken(loginJson("mgr.test@umastar.local", "Password1"), ACCESS_TOKEN_PATTERN);

        mockMvc.perform(post("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "Không Cho Customer",
                                  "email": "staff.cust@umastar.local",
                                  "password": "Password1",
                                  "role": "CUSTOMER"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_STAFF_ROLE.getCode()));

        mockMvc.perform(post("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fullName": "  Chuyên Viên Mới ",
                                  "email": "  Spec.New@UmaStar.Local ",
                                  "password": "Password1",
                                  "phone": "0987654321",
                                  "role": "TRANSPORT_SPECIALIST"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("spec.new@umastar.local"))
                .andExpect(jsonPath("$.role").value("TRANSPORT_SPECIALIST"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andExpect(jsonPath("$.emailVerified").value(true));

        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .param("role", "TRANSPORT_SPECIALIST")
                        .param("status", "ACTIVE")
                        .param("keyword", "spec.new"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].email").value("spec.new@umastar.local"));

        // Khong the tu khoa chinh minh
        mockMvc.perform(patch("/api/v1/users/{id}/status", manager.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "INACTIVE" }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CANNOT_DEACTIVATE_SELF.getCode()));

        // Vo hieu hoa nhan vien -> thu hoi refresh token va chan access token hien tai
        String staffLogin = loginJson("spec.new@umastar.local", "Password1");
        String staffAccess = extractToken(staffLogin, ACCESS_TOKEN_PATTERN);
        String staffRefresh = extractToken(staffLogin, REFRESH_TOKEN_PATTERN);
        User createdStaff = userRepository.findByEmailIgnoreCase("spec.new@umastar.local").orElseThrow();

        mockMvc.perform(patch("/api/v1/users/{id}/status", createdStaff.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "status": "INACTIVE" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        entityManager.flush();
        entityManager.clear();

        RefreshToken revoked = refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(staffRefresh))
                .orElseThrow();
        assertTrue(revoked.isRevoked());
        assertEquals("USER_STATUS_CHANGED", revoked.getRevokedReason());

        mockMvc.perform(get("/api/v1/profile")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + staffAccess))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_DISABLED.getCode()));
    }

    @Test
    void getUnknownUserReturns404() throws Exception {
        User manager = createStaffInDb("unknown.check.mgr@umastar.local", "Quản Lý Kiểm Tra", Role.LOGISTICS_MANAGER);
        String token = extractToken(loginJson(manager.getEmail(), "Password1"), ACCESS_TOKEN_PATTERN);

        mockMvc.perform(get("/api/v1/users/{id}", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.USER_NOT_FOUND.getCode()));
    }

    private User createStaffInDb(String email, String fullName, Role role) {
        User u = new User();
        u.setFullName(fullName);
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("Password1"));
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        u.setEmailVerified(true);
        return userRepository.saveAndFlush(u);
    }

    private String loginJson(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "%s" }
                                """.formatted(email, password)))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
    }

    private String extractToken(String json, Pattern pattern) {
        Matcher matcher = pattern.matcher(json);
        assertTrue(matcher.find());
        return matcher.group(1);
    }
}
