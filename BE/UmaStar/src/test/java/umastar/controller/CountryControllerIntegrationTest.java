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
import umastar.entity.City;
import umastar.entity.Country;
import umastar.entity.User;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.exception.ErrorCode;
import umastar.repository.CityRepository;
import umastar.repository.CountryRepository;
import umastar.repository.UserRepository;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@Transactional
class CountryControllerIntegrationTest {

    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\"accessToken\":\"([^\"]+)\"");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private CountryRepository countryRepository;

    @Autowired
    private CityRepository cityRepository;

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
    void listCountriesRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/countries").param("scope", "DOMESTIC"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    void listCountriesFiltersByScopeAndExcludesInactiveOrNonMatching() throws Exception {
        saveCountry("Z1", "Zeta Home", "Quốc gia sở tại test", true, true, true);
        saveCountry("Z2", "Zeta Partner", "Đối tác test", false, true, true);
        saveCountry("Z3", "Zeta Inactive Partner", "Ngừng hoạt động", false, true, false);
        saveCountry("Z4", "Zeta Non Partner", "Chưa hợp tác", false, false, true);

        String token = loginAsAnyUser("country.viewer@umastar.local", Role.CUSTOMER);

        mockMvc.perform(get("/api/v1/countries")
                        .param("scope", "DOMESTIC")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].isoCode", hasItem("Z1")))
                .andExpect(jsonPath("$[*].isoCode", not(hasItem("Z2"))))
                .andExpect(jsonPath("$[*].isoCode", not(hasItem("Z3"))))
                .andExpect(jsonPath("$[*].isoCode", not(hasItem("Z4"))));

        mockMvc.perform(get("/api/v1/countries")
                        .param("scope", "INTERNATIONAL")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].isoCode", hasItem("Z2")))
                .andExpect(jsonPath("$[*].isoCode", not(hasItem("Z1"))))
                .andExpect(jsonPath("$[*].isoCode", not(hasItem("Z3"))))
                .andExpect(jsonPath("$[*].isoCode", not(hasItem("Z4"))));
    }

    @Test
    void listCountriesRejectsMissingOrInvalidScope() throws Exception {
        String token = loginAsAnyUser("country.badscope@umastar.local", Role.DRIVER);

        mockMvc.perform(get("/api/v1/countries")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.getCode()));

        mockMvc.perform(get("/api/v1/countries")
                        .param("scope", "INVALID_SCOPE")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.getCode()));
    }

    @Test
    void listCitiesRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/v1/countries/{id}/cities", UUID.randomUUID()))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value(ErrorCode.UNAUTHORIZED.getCode()));
    }

    @Test
    void listCitiesReturnsOnlyActiveCitiesOfTargetCountryOrderedByName() throws Exception {
        Country jp = saveCountry("Z5", "Japan", "Nhật Bản", false, true, true);
        Country vn = saveCountry("Z6", "Vietnam", "Việt Nam", true, true, true);
        saveCity(jp, "Tokyo", "Tokyo", "Kanto", "Asia/Tokyo", true, true);
        saveCity(jp, "Kyoto", "Kyoto", "Kansai", "Asia/Tokyo", false, true);
        saveCity(jp, "Osaka", "Osaka", "Kansai", "Asia/Tokyo", true, false);
        saveCity(vn, "Hanoi", "Hà Nội", "Miền Bắc", "Asia/Ho_Chi_Minh", true, true);

        String token = loginAsAnyUser("city.viewer@umastar.local", Role.CUSTOMER);

        mockMvc.perform(get("/api/v1/countries/{id}/cities", jp.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", contains("Kyoto", "Tokyo")))
                .andExpect(jsonPath("$[*].name", not(hasItem("Osaka"))))
                .andExpect(jsonPath("$[*].name", not(hasItem("Hanoi"))))
                .andExpect(jsonPath("$[0].timezone").value("Asia/Tokyo"))
                .andExpect(jsonPath("$[0].hasAirport").value(false))
                .andExpect(jsonPath("$[1].hasAirport").value(true));
    }

    @Test
    void listCitiesRejectsMissingInactiveOrInvalidCountryId() throws Exception {
        Country inactiveCountry = saveCountry("Z7", "Inactive Land", "Ngừng", false, true, false);
        saveCity(inactiveCountry, "Hidden City", "Ẩn", "North", "UTC", true, true);

        String token = loginAsAnyUser("city.errors@umastar.local", Role.DRIVER);

        mockMvc.perform(get("/api/v1/countries/{id}/cities", UUID.randomUUID())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COUNTRY_NOT_FOUND.getCode()));

        mockMvc.perform(get("/api/v1/countries/{id}/cities", inactiveCountry.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.COUNTRY_NOT_FOUND.getCode()));

        mockMvc.perform(get("/api/v1/countries/not-a-uuid/cities")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.INVALID_REQUEST.getCode()));
    }

    private Country saveCountry(String isoCode, String name, String nameVi,
                                boolean homeCountry, boolean partner, boolean active) {
        Country c = new Country();
        c.setIsoCode(isoCode);
        c.setName(name);
        c.setNameVi(nameVi);
        c.setHomeCountry(homeCountry);
        c.setPartner(partner);
        c.setActive(active);
        return countryRepository.saveAndFlush(c);
    }

    private void saveCity(Country country, String name, String nameVi, String region,
                          String timezone, boolean hasAirport, boolean active) {
        City city = new City();
        city.setCountry(country);
        city.setName(name);
        city.setNameVi(nameVi);
        city.setRegion(region);
        city.setTimezone(timezone);
        city.setHasAirport(hasAirport);
        city.setActive(active);
        cityRepository.saveAndFlush(city);
    }

    private String loginAsAnyUser(String email, Role role) throws Exception {
        User u = new User();
        u.setFullName("Viewer");
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
                .andReturn()
                .getResponse()
                .getContentAsString();
        Matcher matcher = ACCESS_TOKEN_PATTERN.matcher(json);
        assertTrue(matcher.find());
        return matcher.group(1);
    }
}

