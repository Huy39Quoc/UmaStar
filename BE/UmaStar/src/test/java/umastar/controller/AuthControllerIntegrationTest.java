package umastar.controller;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import umastar.entity.RefreshToken;
import umastar.entity.User;
import umastar.enums.UserStatus;
import umastar.exception.ErrorCode;
import umastar.repository.CustomerRepository;
import umastar.repository.RefreshTokenRepository;
import umastar.repository.UserRepository;
import umastar.service.MailService;
import umastar.util.TokenHasher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AuthControllerIntegrationTest {

    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\"accessToken\":\"([^\"]+)\"");
    private static final Pattern REFRESH_TOKEN_PATTERN = Pattern.compile("\"refreshToken\":\"([^\"]+)\"");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private EntityManager entityManager;

    @MockitoSpyBean
    private MailService mailService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        reset(mailService);
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void registerSuccessReturns201AndPersistsUserAndCustomer() throws Exception {
        String body = """
                {
                  "fullName": "  Trần Thị Bình ",
                  "email": "  New.Club@UmaStar.Local ",
                  "password": "Password1",
                  "phone": "0912345678",
                  "organizationName": "  CLB Thiên Mã ",
                  "customerType": "CLUB"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").isNotEmpty())
                .andExpect(jsonPath("$.customerId").isNotEmpty())
                .andExpect(jsonPath("$.fullName").value("Trần Thị Bình"))
                .andExpect(jsonPath("$.email").value("new.club@umastar.local"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.organizationName").value("CLB Thiên Mã"))
                .andExpect(jsonPath("$.customerType").value("CLUB"))
                .andExpect(jsonPath("$.passwordHash").doesNotExist());

        User saved = userRepository.findByEmailIgnoreCase("new.club@umastar.local").orElseThrow();
        assertFalse(saved.isEmailVerified());
        assertTrue(customerRepository.existsByUserId(saved.getId()));
    }

    @Test
    void registerDuplicateEmailReturns409() throws Exception {
        String body = """
                {
                  "fullName": "Chủ Ngựa Một",
                  "email": "dup.register@umastar.local",
                  "password": "Password1",
                  "organizationName": "Trang trại A",
                  "customerType": "INDIVIDUAL"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());

        String duplicateBody = """
                {
                  "fullName": "Chủ Ngựa Hai",
                  "email": "  DUP.REGISTER@UmaStar.local ",
                  "password": "Password1",
                  "organizationName": "Trang trại B",
                  "customerType": "CLUB"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateBody))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.EMAIL_ALREADY_EXISTS.getCode()));
    }

    @Test
    void registerInvalidPayloadReturns400WithFieldErrors() throws Exception {
        String body = """
                {
                  "fullName": "",
                  "email": "not-an-email",
                  "password": "weak",
                  "phone": "abc",
                  "organizationName": "",
                  "customerType": null
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()))
                .andExpect(jsonPath("$.errors.fullName").isNotEmpty())
                .andExpect(jsonPath("$.errors.email").isNotEmpty())
                .andExpect(jsonPath("$.errors.password").isNotEmpty())
                .andExpect(jsonPath("$.errors.phone").isNotEmpty())
                .andExpect(jsonPath("$.errors.organizationName").isNotEmpty())
                .andExpect(jsonPath("$.errors.customerType").isNotEmpty());
    }

    @Test
    void registerInvalidEnumValueReturns400InvalidRequest() throws Exception {
        String body = """
                {
                  "fullName": "Nguyễn Văn C",
                  "email": "bad.enum@umastar.local",
                  "password": "Password1",
                  "organizationName": "CLB C",
                  "customerType": "UNKNOWN"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.getCode()));
    }

    @Test
    void loginSuccessReturnsTokensAndPersistsHashedRefreshToken() throws Exception {
        registerAccount("login.ok@umastar.local", "Password1");

        String loginBody = """
                {
                  "email": "  LOGIN.OK@UmaStar.Local ",
                  "password": "Password1"
                }
                """;

        String json = mockMvc.perform(post("/api/v1/auth/login")
                        .header(HttpHeaders.USER_AGENT, "IntegrationTest/1.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.expiresIn").isNumber())
                .andExpect(jsonPath("$.user.email").value("login.ok@umastar.local"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String rawRefreshToken = extractRefreshToken(json);
        RefreshToken stored = refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(rawRefreshToken))
                .orElseThrow();
        assertEquals("login.ok@umastar.local", stored.getUser().getEmail());
        assertFalse(stored.isRevoked());
        assertEquals("IntegrationTest/1.0", stored.getUserAgent());
    }

    @Test
    void loginFiveWrongPasswordsLocksAccountAndRejectsRightPassword() throws Exception {
        registerAccount("lock.test@umastar.local", "Password1");

        String wrongBody = """
                {
                  "email": "lock.test@umastar.local",
                  "password": "WrongPassword1"
                }
                """;

        for (int i = 0; i < 4; i++) {
            mockMvc.perform(post("/api/v1/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(wrongBody))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_CREDENTIALS.getCode()));
        }

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongBody))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_LOCKED.getCode()));

        String rightBody = """
                {
                  "email": "lock.test@umastar.local",
                  "password": "Password1"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(rightBody))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_LOCKED.getCode()));
    }

    @Test
    void loginDisabledAccountReturns403() throws Exception {
        registerAccount("inactive.test@umastar.local", "Password1");
        User user = userRepository.findByEmailIgnoreCase("inactive.test@umastar.local").orElseThrow();
        user.setStatus(UserStatus.INACTIVE);
        userRepository.saveAndFlush(user);

        String loginBody = """
                {
                  "email": "inactive.test@umastar.local",
                  "password": "Password1"
                }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.ACCOUNT_DISABLED.getCode()));
    }

    @Test
    void refreshRotatesTokenAndReuseRevokesEntireFamily() throws Exception {
        registerAccount("refresh.ok@umastar.local", "Password1");
        String firstRawToken = extractRefreshToken(loginJson("refresh.ok@umastar.local", "Password1"));

        String refreshJson = mockMvc.perform(post("/api/v1/auth/refresh")
                        .header(HttpHeaders.USER_AGENT, "IntegrationTest/2.0")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "  %s  " }
                                """.formatted(firstRawToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.refreshToken").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("refresh.ok@umastar.local"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        String secondRawToken = extractRefreshToken(refreshJson);
        assertNotEquals(firstRawToken, secondRawToken);

        RefreshToken firstEntity = refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(firstRawToken))
                .orElseThrow();
        RefreshToken secondEntity = refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(secondRawToken))
                .orElseThrow();
        assertTrue(firstEntity.isRevoked());
        assertEquals("ROTATED", firstEntity.getRevokedReason());
        assertEquals(secondEntity.getId(), firstEntity.getReplacedByTokenId());
        assertEquals(firstEntity.getFamilyId(), secondEntity.getFamilyId());
        assertFalse(secondEntity.isRevoked());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(firstRawToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_REVOKED.getCode()));

        entityManager.flush();
        entityManager.clear();

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(secondRawToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.REFRESH_TOKEN_REVOKED.getCode()));
    }

    @Test
    void refreshUnknownOrBlankTokenReturnsExpectedError() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "non-existent-token" }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REFRESH_TOKEN.getCode()));

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "   " }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()))
                .andExpect(jsonPath("$.errors.refreshToken").isNotEmpty());
    }

    @Test
    void logoutSingleDeviceRevokesOnlyThatSessionAndIsIdempotent() throws Exception {
        registerAccount("logout.one@umastar.local", "Password1");
        String device1Token = extractRefreshToken(loginJson("logout.one@umastar.local", "Password1"));
        String device2Token = extractRefreshToken(loginJson("logout.one@umastar.local", "Password1"));

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(device1Token)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/logout")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(device1Token)))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        RefreshToken revoked = refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(device1Token))
                .orElseThrow();
        assertTrue(revoked.isRevoked());
        assertEquals("LOGOUT", revoked.getRevokedReason());

        mockMvc.perform(post("/api/v1/auth/refresh")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "refreshToken": "%s" }
                                """.formatted(device2Token)))
                .andExpect(status().isOk());
    }

    @Test
    void logoutAllRequiresAuthAndRevokesAllDevices() throws Exception {
        mockMvc.perform(post("/api/v1/auth/logout-all"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));

        registerAccount("logout.all@umastar.local", "Password1");
        String session1Json = loginJson("logout.all@umastar.local", "Password1");
        String accessToken = extractAccessToken(session1Json);
        String refresh1 = extractRefreshToken(session1Json);
        String refresh2 = extractRefreshToken(loginJson("logout.all@umastar.local", "Password1"));

        mockMvc.perform(post("/api/v1/auth/logout-all")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        RefreshToken token1 = refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(refresh1)).orElseThrow();
        RefreshToken token2 = refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(refresh2)).orElseThrow();
        assertTrue(token1.isRevoked());
        assertTrue(token2.isRevoked());
        assertEquals("LOGOUT_ALL", token1.getRevokedReason());
        assertEquals("LOGOUT_ALL", token2.getRevokedReason());
    }

    @Test
    void changePasswordRequiresAuthValidatesRulesAndRevokesSessions() throws Exception {
        mockMvc.perform(post("/api/v1/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "currentPassword": "Password1", "newPassword": "NewPassword2" }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));

        registerAccount("changepw@umastar.local", "Password1");
        String sessionJson = loginJson("changepw@umastar.local", "Password1");
        String accessToken = extractAccessToken(sessionJson);
        String refreshToken = extractRefreshToken(sessionJson);

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "currentPassword": "WrongPassword1", "newPassword": "NewPassword2" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_OLD_PASSWORD.getCode()));

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "currentPassword": "Password1", "newPassword": "Password1" }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.NEW_PASSWORD_SAME_AS_OLD.getCode()));

        mockMvc.perform(post("/api/v1/auth/change-password")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "currentPassword": "Password1", "newPassword": "NewPassword2" }
                                """))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        User updated = userRepository.findByEmailIgnoreCase("changepw@umastar.local").orElseThrow();
        assertNotNull(updated.getPasswordChangedAt());
        assertEquals(0, updated.getFailedLoginAttempts());

        RefreshToken revoked = refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(refreshToken)).orElseThrow();
        assertTrue(revoked.isRevoked());
        assertEquals("PASSWORD_CHANGED", revoked.getRevokedReason());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "changepw@umastar.local", "password": "NewPassword2" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isNotEmpty());
    }

    @Test
    void forgotAndResetPasswordFlowEnforcesRateLimitClearsTokenAndRevokesSessions() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "nobody@umastar.local" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());
        verifyNoInteractions(mailService);

        registerAccount("reset.flow@umastar.local", "Password1");
        String refreshToken = extractRefreshToken(loginJson("reset.flow@umastar.local", "Password1"));

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "  RESET.FLOW@UmaStar.Local " }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());

        ArgumentCaptor<String> rawTokenCaptor = ArgumentCaptor.forClass(String.class);
        verify(mailService).sendPasswordResetEmail(eq("reset.flow@umastar.local"), eq("Người Dùng Kiểm Thử"),
                rawTokenCaptor.capture(), anyLong());
        String rawResetToken = rawTokenCaptor.getValue();

        // Gui lai ngay trong cooldown -> 429 Too Many Requests
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "reset.flow@umastar.local" }
                                """))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.code").value(ErrorCode.TOO_MANY_REQUESTS.getCode()));

        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "%s", "newPassword": "ResetPass2" }
                                """.formatted(rawResetToken)))
                .andExpect(status().isNoContent());

        entityManager.flush();
        entityManager.clear();

        User updated = userRepository.findByEmailIgnoreCase("reset.flow@umastar.local").orElseThrow();
        assertNull(updated.getResetTokenHash());
        assertNull(updated.getResetTokenExpiresAt());
        assertNull(updated.getResetRequestedAt());

        RefreshToken revoked = refreshTokenRepository.findByTokenHash(TokenHasher.sha256Hex(refreshToken)).orElseThrow();
        assertTrue(revoked.isRevoked());
        assertEquals("PASSWORD_RESET", revoked.getRevokedReason());

        // Dung lai token cu -> 400 INVALID_RESET_TOKEN
        mockMvc.perform(post("/api/v1/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "token": "%s", "newPassword": "AnotherPass3" }
                                """.formatted(rawResetToken)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_RESET_TOKEN.getCode()));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "reset.flow@umastar.local", "password": "ResetPass2" }
                                """))
                .andExpect(status().isOk());
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

    private String extractAccessToken(String json) {
        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(json);
        assertTrue(matcher.find());
        return matcher.group(1);
    }

    private String extractRefreshToken(String json) {
        Matcher matcher = REFRESH_TOKEN_PATTERN.matcher(json);
        assertTrue(matcher.find());
        return matcher.group(1);
    }

    private void registerAccount(String email, String password) throws Exception {
        String body = """
                {
                  "fullName": "Người Dùng Kiểm Thử",
                  "email": "%s",
                  "password": "%s",
                  "organizationName": "CLB Test",
                  "customerType": "CLUB"
                }
                """.formatted(email, password);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }
}
