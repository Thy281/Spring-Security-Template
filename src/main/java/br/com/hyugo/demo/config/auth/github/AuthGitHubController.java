package br.com.hyugo.demo.config.auth.github;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/login/github")
public class AuthGitHubController {

    private final LoginGitHubService loginGitHubService;

    public AuthGitHubController(LoginGitHubService loginGitHubService) {
        this.loginGitHubService = loginGitHubService;
    }

    @GetMapping
    public ResponseEntity<Void> redirectToGitHub() {
        HttpHeaders headers = new HttpHeaders();
        var uri = loginGitHubService.gerarUrl();
        headers.setLocation(URI.create(uri));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/auth")
    public ResponseEntity<String> handleGitHubAuth(@RequestParam String code) {
        var email = loginGitHubService.getEmail(code);
        return ResponseEntity.status(HttpStatus.OK).body(email);
    }
}
