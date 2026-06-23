package br.com.hyugo.demo.config.exception;

import com.auth0.jwt.exceptions.JWTCreationException;

public class JwtException extends RuntimeException {
    public JwtException(String message, JWTCreationException ex) {
        super(message, ex);
    }
}
