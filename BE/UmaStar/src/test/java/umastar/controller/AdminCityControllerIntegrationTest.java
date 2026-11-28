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
import umastar.entity.Country;
import umastar.entity.User;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.exception.ErrorCode;
import umastar.repository.CountryRepository;
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
class AdminCityControllerIntegrationTest {

    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\"accessToken\":\"([^\"]+)\"");
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\":\"([^\"]+)\"");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CountryRepository countryRepository;

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
    void adminCityCrudValidatesRoleTimeZoneUniqueNameAndSoftDelete() throws Exception {
        Country jp = saveCountry("Z9", "Japan Test", false, true);
        Country sg = saveCountry("Z0", "Singapore Test", false, true);

        String specialistToken = loginAs("spec.city@umastar.local", Role.TRANSPORT_SPECIALIST);
        mockMvc.perform(get("/api/v1/admin/cities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + specialistToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()));

        String managerToken = loginAs("mgr.city@umastar.local", Role.LOGISTICS_MANAGER);

        mockMvc.perform(post("/api/v1/admin/cities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "countryId": "%s", "name": "BadTZ", "timezone": "UTC+9", "hasAirport": false }
                                """.formatted(jp.getId())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_TIMEZONE.getCode()));

        String createdJson = mockMvc.perform(post("/api/v1/admin/cities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "countryId": "%s", "name": "Chiba", "nameVi": "Chiba", "region": "Kanto", "timezone": "Asia/Tokyo", "hasAirport": true }
                                """.formatted(jp.getId())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.countryName").value("Japan Test"))
                .andExpect(jsonPath("$.active").value(true))
                .andReturn().getResponse().getContentAsString();

        Matcher idMatcher = ID_PATTERN.matcher(createdJson);
        assertTrue(idMatcher.find());
        String cityId = idMatcher.group(1);

        // Duplicate name in same country -> 409
        mockMvc.perform(post("/api/v1/admin/cities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "countryId": "%s", "name": "chiba", "timezone": "Asia/Tokyo", "hasAirport": false }
                                """.formatted(jp.getId())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.CITY_NAME_ALREADY_EXISTS.getCode()));

        // Same name in different country -> 201
        mockMvc.perform(post("/api/v1/admin/cities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "countryId": "%s", "name": "Chiba", "timezone": "Asia/Singapore", "hasAirport": false }
                                """.formatted(sg.getId())))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/admin/cities/{id}", cityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/admin/cities")
                        .param("countryId", jp.getId().toString())
                        .param("active", "false")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].name", hasItem("Chiba")));

        mockMvc.perform(put("/api/v1/admin/cities/{id}", cityId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "name": "Chiba Narita", "nameVi": "Narita", "region": "Kanto", "timezone": "Asia/Tokyo", "hasAirport": true, "active": true }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.name").value("Chiba Narita"));
    }

    private Country saveCountry(String isoCode, String name, boolean homeCountry, boolean partner) {
        Country c = new Country();
        c.setIsoCode(isoCode);
        c.setName(name);
        c.setHomeCountry(homeCountry);
        c.setPartner(partner);
        c.setActive(true);
        return countryRepository.saveAndFlush(c);
    }

    private String loginAs(String email, Role role) throws Exception {
        User u = new User();
        u.setFullName("City Admin Tester");
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

