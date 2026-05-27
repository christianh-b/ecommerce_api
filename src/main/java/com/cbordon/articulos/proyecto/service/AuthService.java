package com.cbordon.articulos.proyecto.service;

import com.cbordon.articulos.proyecto.dto.request.LoginRequest;
import com.cbordon.articulos.proyecto.dto.request.RegisterRequest;
import com.cbordon.articulos.proyecto.dto.response.AuthResponse;

public interface AuthService {
    AuthResponse login(LoginRequest request);
    AuthResponse register(RegisterRequest request);
}
