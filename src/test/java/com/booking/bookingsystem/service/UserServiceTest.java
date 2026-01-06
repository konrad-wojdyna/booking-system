package com.booking.bookingsystem.service;


import com.booking.bookingsystem.dto.request.LoginRequest;
import com.booking.bookingsystem.dto.request.RegisterRequest;
import com.booking.bookingsystem.dto.response.UserResponse;
import com.booking.bookingsystem.exceptions.EmailAlreadyExistsException;
import com.booking.bookingsystem.exceptions.InvalidCredentialsException;
import com.booking.bookingsystem.model.Role;
import com.booking.bookingsystem.model.User;
import com.booking.bookingsystem.repository.UserRepository;
import com.booking.bookingsystem.security.jwt.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtUtils jwtUtils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("Tests for register method")
    class RegisterTest{

        @Test
        @DisplayName("Should successfully register a new user")
        void should_RegisterUser_When_RequestIsValid(){

            //Arrange
            RegisterRequest request = new RegisterRequest("test@example.com", "password", "John");

            User user = User.builder()
                    .id(1L)
                    .email(request.email())
                    .password("encodedPassword")
                    .name("John")
                    .role(Role.USER)
                    .build();

            when(userRepository.existsByEmail(request.email())).thenReturn(false);
            when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
            when(userRepository.save(any(User.class))).thenReturn(user);

            //Act
            UserResponse response = userService.register(request);

            //Assert
            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo(request.email());
            assertThat(response.role()).isEqualTo(Role.USER);
            verify(userRepository, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when email is taken")
        void should_ThrowException_When_EmailExists(){

            //Arrange
            RegisterRequest request = new RegisterRequest("exists@exmaple.com", "password", "John");
            when(userRepository.existsByEmail(request.email())).thenReturn(true);

            //Act & Assert
            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining(request.email());

            verify(userRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tests for login method")
    class LoginTests{

        @Test
        @DisplayName("Should login and return token when credentials are valid")
        void should_Login_When_CredentialsAreValid(){

            //Arrange
            LoginRequest request = new LoginRequest("test@example.com", "password");
            User user = User.builder()
                    .email("test@example.com")
                    .password("encodedPassword")
                    .role(Role.USER)
                    .build();

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
            when(jwtUtils.generateToken(user.getEmail(), "USER")).thenReturn("mock-jwt-token");

            //Act
            UserResponse response = userService.login(request);

            //Assert
            assertThat(response.token()).isEqualTo("mock-jwt-token");
            assertThat(response.email()).isEqualTo(user.getEmail());
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when user not found")
        void should_ThrowException_When_UserNotFound(){

            //Arrange
            LoginRequest request = new LoginRequest("wrong@email.com", "password");
            when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

            //Act & Assert
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }

        @Test
        @DisplayName("Should throw InvalidCredentialsException when password does not match")
        void  should_ThrowException_When_PasswordIsInvalid(){

            //Arrange
            LoginRequest request = new LoginRequest("test@example.com", "wrong-password");
            User user = User.builder()
                    .email("test@example.com")
                    .password("encoded_password")
                    .build();

            when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
            when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

            //Act & Assert
            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(InvalidCredentialsException.class);
        }
    }
}
