package br.com.hyugo.demo.config.auth.google;

import br.com.hyugo.demo.config.auth.OAuthLoginException;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LoginGoogleServiceTest {

    private final LoginGoogleService service = new LoginGoogleService(
            "client-id",
            "client-secret",
            "http://localhost:8080/login/google/auth"
    );

    @Test
    void shouldExtractVerifiedEmailFromGoogleTokenInfo() {
        var email = service.extractEmail(Map.of(
                "aud", "client-id",
                "email_verified", "true",
                "email", "user@email.com"
        ));

        assertThat(email).isEqualTo("user@email.com");
    }

    @Test
    void shouldRejectTokenFromAnotherClientId() {
        assertThatThrownBy(() -> service.extractEmail(Map.of(
                "aud", "other-client-id",
                "email_verified", "true",
                "email", "user@email.com"
        )))
                .isInstanceOf(OAuthLoginException.class)
                .hasMessage("id_token do Google não pertence a este client_id.");
    }

    @Test
    void shouldRejectUnverifiedGoogleEmail() {
        assertThatThrownBy(() -> service.extractEmail(Map.of(
                "aud", "client-id",
                "email_verified", "false",
                "email", "user@email.com"
        )))
                .isInstanceOf(OAuthLoginException.class)
                .hasMessage("E-mail do Google ainda não foi verificado.");
    }
}
