package com.booking.bookingsystem.controller;


import com.booking.bookingsystem.AbstractIntegrationTest;
import com.booking.bookingsystem.dto.request.RegisterRequest;
import com.booking.bookingsystem.model.Role;
import com.booking.bookingsystem.model.User;
import com.booking.bookingsystem.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("AuthController Integration Tests")
public class AuthControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp(){
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register new user and return 201 with user details")
    void shouldRegisterUser_whenValidRequest() throws  Exception{

        //Arrange
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", "Test User");

        //Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.token").doesNotExist());
    }

    @Test
    @DisplayName("Should return 409 when email already exists")
    void shouldReturn409_whenEmailAlreadyExists() throws Exception{

        //Arrange - create existing user in DB
        User existingUser = User.builder()
                .email("existing@example.com")
                .password(passwordEncoder.encode("password"))
                .name("Existing User")
                .role(Role.USER)
                .build();

        userRepository.save(existingUser);

        RegisterRequest request = new RegisterRequest("existing@example.com", "password", "Existing User");

        //Act & Assert
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.code").value("ERR_003"))
                .andExpect(jsonPath("$.message").value(containsString("existing@example.com")));


    }

    @Test
    @DisplayName("Should return 400 when validation fails")
    void shouldReturn400_whenValidationFails() throws Exception {
        // Arrange - invalid email, short password, blank name
        String requestBody = """
            {
                "email": "invalid-email",
                "password": "short",
                "name": ""
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.code").value("ERR_001"))
                .andExpect(jsonPath("$.errors").isMap()) // Field-level errors
                .andExpect(jsonPath("$.errors.email").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.name").exists());
    }

    @Test
    @DisplayName("Should login and return 200 with JWT token")
    void shouldLogin_whenValidCredentials() throws Exception {
        // Arrange - create user in DB
        User user = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("password123"))
                .name("Test User")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        String requestBody = """
            {
                "email": "test@example.com",
                "password": "password123"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.token").exists()) // Token returned on login!
                .andExpect(jsonPath("$.token").isString());
    }

    @Test
    @DisplayName("Should return 401 when credentials are invalid")
    void shouldReturn401_whenInvalidCredentials() throws Exception {
        // Arrange - create user with different password
        User user = User.builder()
                .email("test@example.com")
                .password(passwordEncoder.encode("correctpassword"))
                .name("Test User")
                .role(Role.USER)
                .build();
        userRepository.save(user);

        String requestBody = """
            {
                "email": "test@example.com",
                "password": "wrongpassword"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("ERR_004"))
                .andExpect(jsonPath("$.message").value(containsString("Invalid")));
    }

    @Test
    @DisplayName("Should return 401 when user does not exist")
    void shouldReturn401_whenUserNotFound() throws Exception {
        // Arrange - no user in DB
        String requestBody = """
            {
                "email": "nonexistent@example.com",
                "password": "password123"
            }
            """;

        // Act & Assert
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.code").value("ERR_004"));
    }
}
