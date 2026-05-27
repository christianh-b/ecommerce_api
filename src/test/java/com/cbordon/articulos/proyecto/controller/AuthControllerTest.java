package com.cbordon.articulos.proyecto.controller;

import com.cbordon.articulos.proyecto.dto.request.LoginRequest;
import com.cbordon.articulos.proyecto.dto.request.RegisterRequest;
import com.cbordon.articulos.proyecto.dto.response.AuthResponse;
import com.cbordon.articulos.proyecto.exception.UsernameAlreadyExistsException;
import com.cbordon.articulos.proyecto.security.JwtUtils;
import com.cbordon.articulos.proyecto.security.SecurityConfig;
import com.cbordon.articulos.proyecto.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @Autowired MockMvc mockMvc;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean AuthService authService;
    // Requeridos por JwtAuthFilter que SecurityConfig inyecta en la cadena de filtros
    @MockitoBean JwtUtils jwtUtils;
    @MockitoBean UserDetailsService userDetailsService;

    private static final AuthResponse AUTH_RESPONSE =
            new AuthResponse("header.payload.signature", "testuser", "USER");

    private RegisterRequest buildRegisterRequest(String username, String password) {
        RegisterRequest req = new RegisterRequest();
        req.setUsername(username);
        req.setPassword(password);
        return req;
    }

    private LoginRequest buildLoginRequest(String username, String password) {
        LoginRequest req = new LoginRequest();
        req.setUsername(username);
        req.setPassword(password);
        return req;
    }

    // ── POST /api/auth/register ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/register: datos válidos → 201 con token, username y role")
    void register_valido_returns201ConToken() throws Exception {
        when(authService.register(any())).thenReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRegisterRequest("testuser", "password123"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("header.payload.signature"))
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    @DisplayName("POST /api/auth/register: username ya registrado → 409 Conflict")
    void register_usernameExiste_returns409() throws Exception {
        when(authService.register(any()))
                .thenThrow(new UsernameAlreadyExistsException("testuser"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRegisterRequest("testuser", "pass123"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("POST /api/auth/register: username en blanco → 400 con mensaje de validación")
    void register_usernameBlanco_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRegisterRequest("", "pass123"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("username")));
    }

    @Test
    @DisplayName("POST /api/auth/register: username demasiado corto (< 3 chars) → 400")
    void register_usernameMuyCorto_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRegisterRequest("ab", "pass123"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/register: password demasiado corta (< 6 chars) → 400")
    void register_passwordCorta_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRegisterRequest("validuser", "abc"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("password")));
    }

    @Test
    @DisplayName("POST /api/auth/register: llama al servicio con el username correcto")
    void register_llamaServiceConUsernameCorreecto() throws Exception {
        when(authService.register(any())).thenReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildRegisterRequest("testuser", "password123"))))
                .andExpect(status().isCreated());

        verify(authService).register(argThat(r -> r.getUsername().equals("testuser")));
    }

    // ── POST /api/auth/login ─────────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/auth/login: credenciales válidas → 200 con token")
    void login_valido_returns200ConToken() throws Exception {
        when(authService.login(any())).thenReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildLoginRequest("testuser", "pass"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("header.payload.signature"))
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    @DisplayName("POST /api/auth/login: credenciales incorrectas → 401 con error body")
    void login_credencialesInvalidas_returns401() throws Exception {
        when(authService.login(any())).thenThrow(new BadCredentialsException("bad credentials"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildLoginRequest("user", "wrongpass"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));
    }

    @Test
    @DisplayName("POST /api/auth/login: username en blanco → 400")
    void login_usernameBlanco_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildLoginRequest("", "pass"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login: password en blanco → 400")
    void login_passwordBlanca_returns400() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildLoginRequest("user", ""))))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/auth/login: llama al servicio con las credenciales exactas del request")
    void login_verificaCredencialesEnService() throws Exception {
        when(authService.login(any())).thenReturn(AUTH_RESPONSE);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                buildLoginRequest("user", "pass"))))
                .andExpect(status().isOk());

        verify(authService).login(argThat(r ->
                r.getUsername().equals("user") && r.getPassword().equals("pass")));
    }
}
