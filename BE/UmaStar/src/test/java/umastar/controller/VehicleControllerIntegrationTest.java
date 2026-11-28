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
import umastar.entity.Vehicle;
import umastar.enums.Role;
import umastar.enums.UserStatus;
import umastar.enums.VehicleStatus;
import umastar.enums.VehicleType;
import umastar.exception.ErrorCode;
import umastar.repository.UserRepository;
import umastar.repository.VehicleRepository;

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
class VehicleControllerIntegrationTest {

    private static final Pattern ACCESS_TOKEN_PATTERN = Pattern.compile("\"accessToken\":\"([^\"]+)\"");
    private static final Pattern ID_PATTERN = Pattern.compile("\"id\":\"([^\"]+)\"");

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private VehicleRepository vehicleRepository;

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
    void vehicleCrudEnforcesRoleValidationStatusTransitionsAndDeleteGuards() throws Exception {
        mockMvc.perform(get("/api/v1/vehicles"))
                .andExpect(status().isUnauthorized());

        String managerToken = loginAs("mgr.vehicle@umastar.local", Role.LOGISTICS_MANAGER);
        mockMvc.perform(get("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + managerToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(ErrorCode.FORBIDDEN.getCode()));

        String fleetToken = loginAs("fleet.vehicle@umastar.local", Role.FLEET_COORDINATOR);

        mockMvc.perform(post("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + fleetToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "vehicleType": "HORSE_TRUCK", "codeOrPlate": "51A-BAD0", "capacityHorses": 0 }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(ErrorCode.VALIDATION_FAILED.getCode()));

        String createdJson = mockMvc.perform(post("/api/v1/vehicles")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + fleetToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "vehicleType": "HORSE_TRUCK", "codeOrPlate": "51a-88888", "capacityHorses": 4, "carrierName": "Uma Logistics" }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codeOrPlate").value("51A-88888"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andReturn().getResponse().getContentAsString();

        Matcher idMatcher = ID_PATTERN.matcher(createdJson);
        assertTrue(idMatcher.find());
        String vehicleId = idMatcher.group(1);

        mockMvc.perform(put("/api/v1/vehicles/{id}", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + fleetToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "vehicleType": "HORSE_TRUCK", "codeOrPlate": "51A-88888", "capacityHorses": 5, "carrierName": "Uma Logistics", "status": "MAINTENANCE" }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("MAINTENANCE"))
                .andExpect(jsonPath("$.capacityHorses").value(5));

        mockMvc.perform(get("/api/v1/vehicles")
                        .param("status", "MAINTENANCE")
                        .param("keyword", "88888")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + fleetToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].codeOrPlate", hasItem("51A-88888")));

        Vehicle inUse = new Vehicle();
        inUse.setVehicleType(VehicleType.AIR_STALL);
        inUse.setCodeOrPlate("STALL-BUSY");
        inUse.setCapacityHorses(2);
        inUse.setStatus(VehicleStatus.IN_USE);
        inUse = vehicleRepository.saveAndFlush(inUse);

        mockMvc.perform(put("/api/v1/vehicles/{id}", inUse.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + fleetToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                { "vehicleType": "AIR_STALL", "codeOrPlate": "STALL-BUSY", "capacityHorses": 2, "status": "MAINTENANCE" }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.VEHICLE_IN_USE.getCode()));

        mockMvc.perform(delete("/api/v1/vehicles/{id}", inUse.getId())
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + fleetToken))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(ErrorCode.VEHICLE_IN_USE.getCode()));

        mockMvc.perform(delete("/api/v1/vehicles/{id}", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + fleetToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/vehicles/{id}", vehicleId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + fleetToken))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(ErrorCode.VEHICLE_NOT_FOUND.getCode()));
    }

    private String loginAs(String email, Role role) throws Exception {
        User u = new User();
        u.setFullName("Vehicle Tester");
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

