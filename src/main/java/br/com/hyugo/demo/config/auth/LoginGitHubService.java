package br.com.hyugo.demo.config.auth;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class LoginGitHubService {

    private final String CLIENT_ID = "Ov23liKuIC66velabRy5";
    private final String CLIENT_SECRET = "22c9108cff2e6058e34a5e3ac324d88e2d4e06da";
    private final String SCOPE = "read:user,user:email";
    private final String REDIRECT_URI = "http://localhost:8080/login/github/auth";
    private final RestClient restClient;

    public LoginGitHubService() {
        this.restClient = RestClient.builder().build();
    }

    public String gerarUrl() {
        return "https://github.com/login/oauth/authorize?client_id="
                + CLIENT_ID + "&redirect_uri=" + REDIRECT_URI + "&scope=" + SCOPE;

    }

    private String getToken(String code) {
        var request = restClient.post()
                .uri("https://github.com/login/oauth/access_token?")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(Map.of(
                        "client_id", CLIENT_ID,
                        "client_secret", CLIENT_SECRET,
                        "code", code,
                        "redirect_uri", REDIRECT_URI
                ))
                .retrieve()
                .body(Map.class);
        return request.get("access_token").toString();
    }

    public String getEmail(String code) {
        var token = getToken(code);
        var headers = new HttpHeaders();
        headers.setBearerAuth(token);
        var emails = restClient.get()
                .uri("https://api.github.com/user/emails")
                .headers(h -> h.addAll(headers))
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                });
        return extractEmail(emails);
    }

    String extractEmail(List<Map<String, Object>> emails) {
        if (emails == null || emails.isEmpty()) {
            throw new IllegalStateException("GitHub did not return any email address");
        }

        return emails.stream()
                .filter(email -> Boolean.TRUE.equals(email.get("primary")))
                .filter(email -> Boolean.TRUE.equals(email.get("verified")))
                .findFirst()
                .or(() -> emails.stream()
                        .filter(email -> Boolean.TRUE.equals(email.get("verified")))
                        .findFirst())
                .or(() -> emails.stream().findFirst())
                .map(email -> email.get("email"))
                .map(Object::toString)
                .orElseThrow(() -> new IllegalStateException("GitHub email response does not contain an email"));
    }
}
