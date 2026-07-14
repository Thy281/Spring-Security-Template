package br.com.hyugo.demo.config.auth.github;

import br.com.hyugo.demo.config.auth.OAuthLoginException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;

@Service
public class LoginGitHubService {

    private static final String TOKEN_URL = "https://github.com/login/oauth/access_token";
    private static final String EMAILS_URL = "https://api.github.com/user/emails";
    private static final String AUTHORIZE_URL = "https://github.com/login/oauth/authorize";

    private final String clientId;
    private final String clientSecret;
    private final String scope;
    private final String redirectUri;
    private final RestClient restClient;

    public LoginGitHubService(
            @Value("${github.oauth.client-id}") String clientId,
            @Value("${github.oauth.client-secret}") String clientSecret,
            @Value("${github.oauth.scope}") String scope,
            @Value("${github.oauth.redirect-uri}") String redirectUri
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
        this.redirectUri = redirectUri;
        this.restClient = RestClient.builder().build();
    }

    public String gerarUrl() {
        return UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("scope", scope)
                .toUriString();
    }

    private String getToken(String code) {
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("code", code);
            body.add("redirect_uri", redirectUri);

            var response = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response == null || response.get("access_token") == null) {
                throw new OAuthLoginException("GitHub não retornou o access token.");
            }

            return response.get("access_token").toString();
        } catch (RestClientException ex) {
            throw new OAuthLoginException("Não foi possível obter o token do GitHub.");
        }
    }

    public String getEmail(String code) {
        try {
            var token = getToken(code);
            var headers = new HttpHeaders();
            headers.setBearerAuth(token);
            var emails = restClient.get()
                    .uri(EMAILS_URL)
                    .headers(h -> h.addAll(headers))
                    .retrieve()
                    .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                    });
            return extractEmail(emails);
        } catch (RestClientException ex) {
            throw new OAuthLoginException("Não foi possível buscar o e-mail do GitHub.");
        }
    }

    String extractEmail(List<Map<String, Object>> emails) {
        if (emails == null || emails.isEmpty()) {
            throw new OAuthLoginException("GitHub não retornou nenhum e-mail.");
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
                .orElseThrow(() -> new OAuthLoginException("Resposta do GitHub não contém e-mail."));
    }
}
