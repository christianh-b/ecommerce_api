package com.cbordon.articulos.proyecto.service;

import com.cbordon.articulos.proyecto.dto.request.LoginRequest;
import com.cbordon.articulos.proyecto.dto.request.RegisterRequest;
import com.cbordon.articulos.proyecto.dto.response.AuthResponse;
import com.cbordon.articulos.proyecto.exception.UsernameAlreadyExistsException;
import com.cbordon.articulos.proyecto.model.Role;
import com.cbordon.articulos.proyecto.model.Usuario;
import com.cbordon.articulos.proyecto.repository.UsuarioRepository;
import com.cbordon.articulos.proyecto.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final AuthenticationManager authenticationManager;

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow();
        String token = jwtUtils.generateToken(usuario);
        return new AuthResponse(token, usuario.getUsername(), usuario.getRole().name());
    }

    @Override
    public AuthResponse register(RegisterRequest request) {
        if (usuarioRepository.existsByUsername(request.getUsername())) {
            throw new UsernameAlreadyExistsException(request.getUsername());
        }
        Usuario usuario = new Usuario(
                null,
                request.getUsername(),
                passwordEncoder.encode(request.getPassword()),
                Role.USER
        );
        usuarioRepository.save(usuario);
        String token = jwtUtils.generateToken(usuario);
        return new AuthResponse(token, usuario.getUsername(), usuario.getRole().name());
    }
}
