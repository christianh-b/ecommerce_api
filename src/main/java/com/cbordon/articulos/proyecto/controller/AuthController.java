package com.cbordon.articulos.proyecto.controller;

import com.cbordon.articulos.proyecto.dto.request.LoginRequest;
import com.cbordon.articulos.proyecto.dto.request.RegisterRequest;
import com.cbordon.articulos.proyecto.dto.response.AuthResponse;
import com.cbordon.articulos.proyecto.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Autenticación", description = "Registro e inicio de sesión de usuarios")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Registrar usuario", description = "Crea una cuenta nueva con ROLE_USER y retorna un token JWT.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Usuario registrado — retorna token JWT"),
        @ApiResponse(responseCode = "400", description = "Datos de entrada inválidos"),
        @ApiResponse(responseCode = "409", description = "El username ya está registrado")
    })
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @Operation(summary = "Iniciar sesión", description = "Autentica las credenciales y retorna un token JWT.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Login exitoso — retorna token JWT"),
        @ApiResponse(responseCode = "400", description = "Username o password vacíos"),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
