package umastar.controller;

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
import umastar.entity.User;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.exception.ErrorCode;
import umastar.repository.UserRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class AdminCountryControllerIntegrationTest {

    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\"accessToken\":\"([^\"]+)\"");
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\":\"([^\"]+)\"");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void adminCountryCrudEnforcesManagerRoleValidationAndSoftDelete() throws Exception {
        mockMvc.perform(get("/api/v1/admin/countries"))
                .andExpect(status().isUnauthorized());

        String coordinatorToken = loginAs("coord.country@umastar.local", Role.FLEET_COORDINATOR);
        mockMvc.perform(get("/api/v1/admin/countries")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + coordinatorToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()));

        String managerToken = loginAs("mgr.country@umastar.local", Role.LOGISTICS_MANAGER);

        String createdJson = mockMvc.perform(post("/api/v1/admin/countries")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "isoCode": "zx", "name": "Testonia", "nameVi": "Xứ Test", "homeCountry": false, "partner": true }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.isoCode").value("ZX"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();

        Matcher idMatcher = ID_PATTERN.matcher(createdJson);
        assertTrue(idMatcher.find());
        String countryId = idMatcher.group(1);

        mockMvc.perform(post("/api/v1/admin/countries")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "isoCode": "ZX", "name": "Duplicate", "homeCountry": false, "partner": true }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.COUNTRY_CODE_ALREADY_EXISTS.getCode()));

        mockMvc.perform(delete("/api/v1/admin/countries/{id}", countryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/countries/{id}", countryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(get("/api/v1/admin/countries")
                        .param("keyword", "ZX")
                        .param("active", "false")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].isoCode", hasItem("ZX")));

        mockMvc.perform(put("/api/v1/admin/countries/{id}", countryId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "isoCode": "ZX", "name": "Testonia Reactivated", "nameVi": "Xứ Test 2", "homeCountry": false, "partner": true, "active": true }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.name").value("Testonia Reactivated"));
    }

    private String loginAs(String email, Role role) throws Exception {
        User u = new User();
        u.setFullName("Admin Tester");
        u.setEmail(email);
        u.setPasswordHash(passwordEncoder.encode("Password1"));
        u.setRole(role);
        u.setStatus(UserStatus.ACTIVE);
        u.setEmailVerified(true);
        userRepository.saveAndFlush(u);

        String json = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "email": "%s", "password": "Password1" }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(json);
        assertTrue(matcher.find());
        return matcher.group(1);
    }
}

