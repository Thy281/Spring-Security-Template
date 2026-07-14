package br.com.hyugo.demo.config.auth.github;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class LoginGitHubServiceTest {

    private final LoginGitHubService service = new LoginGitHubService(
            "client-id",
            "client-secret",
            "read:user,user:email",
            "http://localhost:8080/login/github/auth"
    );

    @Test
    void shouldExtractPrimaryVerifiedEmailFromGitHubEmailList() {
        var email = service.extractEmail(List.of(
                Map.of("email", "secondary@email.com", "primary", false, "verified", true),
                Map.of("email", "primary@email.com", "primary", true, "verified", true)
        ));

        assertThat(email).isEqualTo("primary@email.com");
    }

    @Test
    void shouldFallbackToVerifiedEmailWhenPrimaryEmailIsNotVerified() {
        var email = service.extractEmail(List.of(
                Map.of("email", "primary@email.com", "primary", true, "verified", false),
                Map.of("email", "verified@email.com", "primary", false, "verified", true)
        ));

        assertThat(email).isEqualTo("verified@email.com");
    }
}
