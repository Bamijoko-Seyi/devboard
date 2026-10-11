package com.devboard.service;

import com.devboard.dto.LoginRequest;
import com.devboard.dto.LoginResponse;
import com.devboard.dto.RegisterRequest;
import com.devboard.dto.UserResponse;
import com.devboard.entity.User;
import com.devboard.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
    }

    @Test
    void register_createsUserWithHashedPassword() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("new@example.com");
        request.setPassword("password123");
        request.setDisplayName("New User");

        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            u.setCreatedAt(LocalDateTime.now());
            return u;
        });

        UserResponse response = authService.register(request);

        assertThat(response.getEmail()).isEqualTo("new@example.com");
        assertThat(response.getDisplayName()).isEqualTo("New User");

        verify(userRepository).save(argThat(u ->
                u.getPassword().startsWith("$2a$") &&
                        !u.getPassword().equals("password123")
        ));
    }

    @Test
    void register_throwsWhenEmailExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("existing@example.com");
        request.setPassword("password123");
        request.setDisplayName("Existing");

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(EmailAlreadyExistsException.class)
                .hasMessageContaining("existing@example.com");

        verify(userRepository, never()).save(any());
    }

    @Test
    void login_returnsTokenOnValidCredentials() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("correct"));
        user.setDisplayName("User");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(1L, "user@example.com")).thenReturn("fake.jwt.token");

        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("correct");

        LoginResponse response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("fake.jwt.token");
    }

    @Test
    void login_throwsOnWrongPassword() {
        User user = new User();
        user.setId(1L);
        user.setEmail("user@example.com");
        user.setPassword(passwordEncoder.encode("correct"));
        user.setDisplayName("User");

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));

        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("wrong");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(jwtService, never()).generateToken(any(), anyString());
    }

    @Test
    void login_throwsOnUnknownEmail() {
        when(userRepository.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

        LoginRequest req = new LoginRequest();
        req.setEmail("nobody@example.com");
        req.setPassword("whatever");

        assertThatThrownBy(() -> authService.login(req))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}