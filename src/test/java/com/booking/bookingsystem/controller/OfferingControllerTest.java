package com.booking.bookingsystem.controller;

import com.booking.bookingsystem.AbstractIntegrationTest;
import com.booking.bookingsystem.dto.request.CreateOfferingRequest;
import com.booking.bookingsystem.dto.request.LoginRequest;
import com.booking.bookingsystem.dto.request.UpdateOfferingRequest;
import com.booking.bookingsystem.model.Role;
import com.booking.bookingsystem.model.User;
import com.booking.bookingsystem.repository.OfferingRepository;
import com.booking.bookingsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@DisplayName("OfferingController Integration Tests")
public class OfferingControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfferingRepository offeringRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String adminToken;

    @BeforeEach
    void setIp() throws Exception{
        offeringRepository.deleteAll();
        userRepository.deleteAll();

        //Create admin user
        User admin = User.builder()
                .email("admin@salon.com")
                .password(passwordEncoder.encode("pass123"))
                .name("Admin")
                .role(Role.ADMIN)
                .build();

        userRepository.save(admin);

        //Login to get token
        LoginRequest loginRequest = new LoginRequest("admin@salon.com", "pass123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    void createOffering_WithAdminToken_ShouldReturnCreated() throws Exception{
        CreateOfferingRequest request = new CreateOfferingRequest(
                "Haircut", "Standard Haircut", 30, BigDecimal.valueOf(50.00)
        );

        mockMvc.perform(post("/api/services")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.price").value(request.price()));
    }

    @Test
    void createOffering_WithoutToken_ShouldReturnForbidden() throws Exception {
        CreateOfferingRequest request = new CreateOfferingRequest(
                "Haircut", "Standard Haircut", 30, BigDecimal.valueOf(50.00)
        );

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());  // 403 (anonymous access denied)
    }

    @Test
    void createOffering_WithUserToken_ShouldReturnForbidden() throws Exception{
        //Given
        User user = User.builder()
                .email("user@salon.com")
                .password(passwordEncoder.encode("pass123"))
                .name("Regular User")
                .role(Role.USER)
                .build();

        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("user@salon.com", "pass123");
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String userToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();

        //When: USER tries to create offering
        CreateOfferingRequest request = new CreateOfferingRequest(
                "Haircut", "Standard Haircut", 30, BigDecimal.valueOf(50.00)
        );

        //Then: Should be FORBIDDEN
        mockMvc.perform(post("/api/services")
                .header("Authorization", "Bearer " + userToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"));
    }

    @Test
    void getAllOfferings_WithoutToken_ShouldReturnOk() throws Exception{
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getOfferingById_WhenExists_ShouldReturnOk() throws Exception{
        CreateOfferingRequest request = new CreateOfferingRequest(
                "Haircut", "Standard Haircut", 30, BigDecimal.valueOf(50.00)
        );

        MvcResult result = mockMvc.perform(post("/api/services")
                .header("Authorization", "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        Long createdId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(get("/api/services/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.name").value(request.name()))
                .andExpect(jsonPath("$.price").value(request.price()));
    }

    @Test
    void getOfferingById_WhenNotExists_ShouldReturnNotFound() throws Exception{
        mockMvc.perform(get("/api/services/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateOffering_WithAdminToken_ShouldReturnOk() throws Exception {
        // Given: Create offering
        CreateOfferingRequest createRequest = new CreateOfferingRequest(
                "Haircut", "Standard Haircut", 30, BigDecimal.valueOf(50.00)
        );

        MvcResult createResult = mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long createdId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // When: Update offering
        UpdateOfferingRequest updateRequest = new UpdateOfferingRequest(
                "Haircut Premium", "Premium haircut with styling", 45, BigDecimal.valueOf(75.00)
        );

        // Then: Should update successfully
        mockMvc.perform(put("/api/services/" + createdId)
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.name").value("Haircut Premium"))
                .andExpect(jsonPath("$.price").value(75.00));
    }

    @Test
    void updateOffering_WithUserToken_ShouldReturnForbidden() throws Exception {
        // Given: Create offering as ADMIN
        CreateOfferingRequest createRequest = new CreateOfferingRequest(
                "Haircut", "Standard Haircut", 30, BigDecimal.valueOf(50.00)
        );

        MvcResult createResult = mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long createdId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Create USER and get token
        User user = User.builder()
                .email("user@salon.com")
                .password(passwordEncoder.encode("pass123"))
                .name("Regular User")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("user@salon.com", "pass123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String userToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // When: USER tries to update
        UpdateOfferingRequest updateRequest = new UpdateOfferingRequest(
                "Haircut Premium", "Premium haircut", 45, BigDecimal.valueOf(75.00)
        );

        // Then: Should be FORBIDDEN
        mockMvc.perform(put("/api/services/" + createdId)
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOffering_WhenNotFound_ShouldReturnNotFound() throws Exception {
        UpdateOfferingRequest updateRequest = new UpdateOfferingRequest(
                "Haircut Premium", "Premium haircut", 45, BigDecimal.valueOf(75.00)
        );

        mockMvc.perform(put("/api/services/999")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOffering_WithAdminToken_ShouldReturnNoContent() throws Exception {
        // Given: Create offering
        CreateOfferingRequest createRequest = new CreateOfferingRequest(
                "Haircut", "Standard Haircut", 30, BigDecimal.valueOf(50.00)
        );

        MvcResult createResult = mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long createdId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // When: Delete offering (soft delete)
        mockMvc.perform(delete("/api/services/" + createdId)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        // Then: Verify soft delete (not in active list, but still exists by ID)
        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$").isEmpty());  // Not in active list

        mockMvc.perform(get("/api/services/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));  // Still exists, but active=false
    }

    @Test
    void deleteOffering_WithUserToken_ShouldReturnForbidden() throws Exception {
        // Given: Create offering as ADMIN
        CreateOfferingRequest createRequest = new CreateOfferingRequest(
                "Haircut", "Standard Haircut", 30, BigDecimal.valueOf(50.00)
        );

        MvcResult createResult = mockMvc.perform(post("/api/services")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        Long createdId = objectMapper.readTree(createResult.getResponse().getContentAsString())
                .get("id").asLong();

        // Create USER and get token
        User user = User.builder()
                .email("user@salon.com")
                .password(passwordEncoder.encode("pass123"))
                .name("Regular User")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        LoginRequest loginRequest = new LoginRequest("user@salon.com", "pass123");
        MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String userToken = objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token").asText();

        // When: USER tries to delete
        mockMvc.perform(delete("/api/services/" + createdId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteOffering_WithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(delete("/api/services/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteOffering_WhenNotFound_ShouldReturnNotFound() throws Exception {
        mockMvc.perform(delete("/api/services/999")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNotFound());
    }

}
