package com.devboard.controller;

import com.devboard.dto.LoginResponse;
import com.devboard.dto.UserResponse;
import com.devboard.service.AuthService;
import com.devboard.service.EmailAlreadyExistsException;
import com.devboard.service.InvalidCredentialsException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AuthService authService;

    @Test
    void register_returns201OnSuccess() throws Exception {
        UserResponse response = new UserResponse(1L, "new@example.com", "New", LocalDateTime.now());
        when(authService.register(any())).thenReturn(response);

        String body = objectMapper.writeValueAsString(Map.of(
                "email", "new@example.com",
                "password", "password123",
                "displayName", "New"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new@example.com"))
                .andExpect(jsonPath("$.displayName").value("New"));
    }

    @Test
    void register_returns400OnInvalidEmail() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "email", "not-an-email",
                "password", "password123",
                "displayName", "New"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void register_returns409OnDuplicateEmail() throws Exception {
        when(authService.register(any()))
                .thenThrow(new EmailAlreadyExistsException("existing@example.com"));

        String body = objectMapper.writeValueAsString(Map.of(
                "email", "existing@example.com",
                "password", "password123",
                "displayName", "New"
        ));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(
                        org.hamcrest.Matchers.containsString("existing@example.com")));
    }

    @Test
    void login_returns401OnBadCredentials() throws Exception {
        when(authService.login(any())).thenThrow(new InvalidCredentialsException());

        String body = objectMapper.writeValueAsString(Map.of(
                "email", "user@example.com",
                "password", "wrong"
        ));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
}