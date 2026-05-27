package com.cbordon.articulos.proyecto.service;

import com.cbordon.articulos.proyecto.dto.request.LoginRequest;
import com.cbordon.articulos.proyecto.dto.request.RegisterRequest;
import com.cbordon.articulos.proyecto.dto.response.AuthResponse;
import com.cbordon.articulos.proyecto.exception.UsernameAlreadyExistsException;
import com.cbordon.articulos.proyecto.model.Role;
import com.cbordon.articulos.proyecto.model.Usuario;
import com.cbordon.articulos.proyecto.repository.UsuarioRepository;
import com.cbordon.articulos.proyecto.security.JwtUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock UsuarioRepository usuarioRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtils jwtUtils;
    @Mock AuthenticationManager authenticationManager;

    @InjectMocks AuthServiceImpl authService;

    private static final String FAKE_TOKEN = "header.payload.signature";
    private static final Usuario USUARIO = new Usuario(1L, "admin", "encoded_pass", Role.ADMIN);

    // ── login ────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("login: credenciales válidas → retorna AuthResponse con token, username y role")
    void login_credencialesValidas_retornaAuthResponse() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("pass");

        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(USUARIO));
        when(jwtUtils.generateToken(USUARIO)).thenReturn(FAKE_TOKEN);

        AuthResponse response = authService.login(request);

        assertThat(response.getToken()).isEqualTo(FAKE_TOKEN);
        assertThat(response.getUsername()).isEqualTo("admin");
        assertThat(response.getRole()).isEqualTo("ADMIN");
    }

    @Test
    @DisplayName("login: verifica que llama al AuthenticationManager con las credenciales del request")
    void login_verificaCredencialesEnAuthManager() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("secret");

        when(authenticationManager.authenticate(any())).thenReturn(mock(Authentication.class));
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(USUARIO));
        when(jwtUtils.generateToken(any())).thenReturn(FAKE_TOKEN);

        authService.login(request);

        verify(authenticationManager).authenticate(
                argThat(auth ->
                        auth.getPrincipal().equals("admin") &&
                        auth.getCredentials().equals("secret"))
        );
    }

    @Test
    @DisplayName("login: credenciales incorrectas → propaga BadCredentialsException sin buscar al usuario")
    void login_credencialesInvalidas_propagaExcepcion() {
        LoginRequest request = new LoginRequest();
        request.setUsername("admin");
        request.setPassword("wrong");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad credentials"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
        verify(usuarioRepository, never()).findByUsername(any());
    }

    // ── register ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("register: usuario nuevo → guarda con Role.USER y retorna token")
    void register_usuarioNuevo_guardaConRoleUserYRetornaToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("newuser");
        request.setPassword("password123");

        when(usuarioRepository.existsByUsername("newuser")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("hashed_password");
        when(jwtUtils.generateToken(any())).thenReturn(FAKE_TOKEN);

        AuthResponse response = authService.register(request);

        verify(usuarioRepository).save(argThat(u ->
                u.getUsername().equals("newuser") &&
                u.getPassword().equals("hashed_password") &&
                u.getRole() == Role.USER
        ));
        assertThat(response.getToken()).isEqualTo(FAKE_TOKEN);
        assertThat(response.getRole()).isEqualTo("USER");
    }

    @Test
    @DisplayName("register: encripta la contraseña antes de persistir")
    void register_encodesPasswordAntesDeGuardar() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("user");
        request.setPassword("rawPass");

        when(usuarioRepository.existsByUsername("user")).thenReturn(false);
        when(passwordEncoder.encode("rawPass")).thenReturn("hashed");
        when(jwtUtils.generateToken(any())).thenReturn(FAKE_TOKEN);

        authService.register(request);

        verify(passwordEncoder).encode("rawPass");
    }

    @Test
    @DisplayName("register: username ya existe → lanza IllegalArgumentException, no guarda ni encripta")
    void register_usuarioExistente_lanzaIllegalArgumentException() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setPassword("pass123");

        when(usuarioRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameAlreadyExistsException.class)
                .hasMessageContaining("existing");
        verify(usuarioRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    @DisplayName("register: username ya existe → no genera token JWT")
    void register_usuarioExistente_noGeneraToken() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("existing");
        request.setPassword("pass123");

        when(usuarioRepository.existsByUsername("existing")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(UsernameAlreadyExistsException.class);
        verify(jwtUtils, never()).generateToken(any());
    }
}
