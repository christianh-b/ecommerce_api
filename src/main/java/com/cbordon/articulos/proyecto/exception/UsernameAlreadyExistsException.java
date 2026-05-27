package com.cbordon.articulos.proyecto.exception;

public class UsernameAlreadyExistsException extends RuntimeException {
    public UsernameAlreadyExistsException(String username) {
        super("El usuario '" + username + "' ya está registrado");
    }
}
