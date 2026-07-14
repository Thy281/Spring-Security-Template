package br.com.hyugo.demo.config.auth.google;

import br.com.hyugo.demo.config.auth.OAuthLoginException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Service
public class LoginGoogleService {

    private static final String TOKEN_INFO_URL = "https://oauth2.googleapis.com/tokeninfo";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";

    private final String clientId;
    private final String clientSecret;
    private final String redirectUri;
    private final RestClient restClient;

    public LoginGoogleService(
            @Value("${google.oauth.client-id}") String clientId,
            @Value("${google.oauth.client-secret}") String clientSecret,
            @Value("${google.oauth.redirect-uri}") String redirectUri
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.redirectUri = redirectUri;
        this.restClient = RestClient.builder().build();
    }

    public String gerarUrl() {
        return org.springframework.web.util.UriComponentsBuilder.fromUriString(AUTHORIZE_URL)
                .queryParam("client_id", clientId)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("response_type", "code")
                .queryParam("scope", "openid email profile")
                .toUriString();
    }

    public String getEmailByCode(String code) {
        return getEmail(getIdToken(code));
    }

    private String getIdToken(String code) {
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("client_id", clientId);
            body.add("client_secret", clientSecret);
            body.add("code", code);
            body.add("redirect_uri", redirectUri);
            body.add("grant_type", "authorization_code");

            var response = restClient.post()
                    .uri(TOKEN_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            if (response == null || response.get("id_token") == null) {
                throw new OAuthLoginException("Google não retornou o id_token.");
            }

            return response.get("id_token").toString();
        } catch (RestClientException ex) {
            throw new OAuthLoginException("Não foi possível obter o token do Google.");
        }
    }

    public String getEmail(String idToken) {
        try {
            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("id_token", idToken);

            var tokenInfo = restClient.post()
                    .uri(TOKEN_INFO_URL)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .accept(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<Map<String, Object>>() {
                    });

            return extractEmail(tokenInfo);
        } catch (RestClientException ex) {
            throw new OAuthLoginException("Não foi possível validar o login com o Google.");
        }
    }

    String extractEmail(Map<String, Object> tokenInfo) {
        if (tokenInfo == null || tokenInfo.isEmpty()) {
            throw new OAuthLoginException("Google não retornou dados do id_token.");
        }

        var aud = valueOf(tokenInfo.get("aud"));
        if (!clientId.equals(aud)) {
            throw new OAuthLoginException("id_token do Google não pertence a este client_id.");
        }

        if (!Boolean.parseBoolean(valueOf(tokenInfo.get("email_verified")))) {
            throw new OAuthLoginException("E-mail do Google ainda não foi verificado.");
        }

        var email = valueOf(tokenInfo.get("email"));
        if (email.isBlank()) {
            throw new OAuthLoginException("Resposta do Google não contém e-mail.");
        }

        return email;
    }

    private String valueOf(Object value) {
        return value == null ? "" : value.toString();
    }
}
