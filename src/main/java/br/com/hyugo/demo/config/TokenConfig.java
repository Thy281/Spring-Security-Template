package br.com.hyugo.demo.config;

import br.com.hyugo.demo.config.exception.JwtException;
import br.com.hyugo.demo.entity.Role;
import br.com.hyugo.demo.entity.User;
import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Optional;

@Component
public class TokenConfig {

    private final String secret;
    private final Long expiration;

    public TokenConfig(
            @Value("${jwt.secret:${jwt.key:}}") String secret,
            @Value("${jwt.expiration}") Long expiration
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("Configure jwt.secret ou jwt.key antes de iniciar a aplicação.");
        }
        this.secret = secret;
        this.expiration = expiration;
    }

    public String generateToken(User user) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    .withClaim("userId", user.getId())
                    .withClaim("role", user.getRole().name())
                    .withSubject(user.getEmail())
                    .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                    .sign(algorithm);

        } catch (JWTCreationException ex) {
            throw new JwtException("Erro ao gerar token JWT", ex);
        }
    }

    public Optional<JWTUserData> validateToken(String token) {
        try {
            Algorithm algorithm = Algorithm.HMAC256(secret);

            DecodedJWT decodedJWT = JWT.require(algorithm)
                    .build().verify(token);

            Long userId = decodedJWT.getClaim("userId").asLong();
            String email = decodedJWT.getSubject();
            String roleClaim = decodedJWT.getClaim("role").asString();
            if (roleClaim == null || roleClaim.isBlank()) {
                return Optional.empty();
            }
            Role role = Role.valueOf(roleClaim);
            return Optional.of(new JWTUserData(userId, email, role));
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        } catch (IllegalArgumentException ex) {
            return Optional.empty();
        }
    }
}
