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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@AllArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;

    public UserResponse register(RegisterRequest request){

        log.info("Registration attempt for email: {}", request.email());

        if(userRepository.existsByEmail(request.email())){
            log.warn("Registration failed: Email already exists - {}", request.email());
            throw new EmailAlreadyExistsException(request.email());
        }

        String password = passwordEncoder.encode(request.password());

        User user =  User.builder()
                .email(request.email())
                .password(password)
                .name(request.name())
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        log.info("User registered successfully: {} with ID: {}",
                savedUser.getEmail(), savedUser.getId());

        return UserResponse.from(
                savedUser,
                null,
                user.getRole()
        );
    }

    public UserResponse login(LoginRequest request){

        log.info("Login attempt for email: {}", request.email());

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> {
                    log.warn("Login failed: User not found - {}", request.email());
                    return new InvalidCredentialsException();
                });

        if(!passwordEncoder.matches(request.password(), user.getPassword())){
            log.warn("Login failed: Invalid password for user - {}", request.email());
            throw new InvalidCredentialsException();
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole().name());

        log.info("User logged in successfully: {}", user.getEmail());

        return UserResponse.from(
                user,
                token,
                user.getRole()
        );
    }
}
