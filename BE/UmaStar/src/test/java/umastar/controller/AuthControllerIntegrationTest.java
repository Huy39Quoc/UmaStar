package umastar.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
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
import umastar.util.TokenHasher;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AuthControllerIntegrationTest {

    private static final Pattern REFRESH_TOKEN_PATTERN = Pattern.compile("\"refreshToken\":\"([^\"]+)\"");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
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

        Matcher matcher = REFRESH_TOKEN_PATTERN.matcher(json);
        assertTrue(matcher.find());
        String rawRefreshToken = matcher.group(1);

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
