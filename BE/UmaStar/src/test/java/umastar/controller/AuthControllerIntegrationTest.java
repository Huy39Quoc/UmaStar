package umastar.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import umastar.entity.User;
import umastar.exception.ErrorCode;
import umastar.repository.CustomerRepository;
import umastar.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AuthControllerIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

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
}

