package br.com.hyugo.demo.config.auth.google;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/login/google")
public class AuthGoogleController {

    private final LoginGoogleService loginGoogleService;

    public AuthGoogleController(LoginGoogleService loginGoogleService) {
        this.loginGoogleService = loginGoogleService;
    }

    @GetMapping
    public ResponseEntity<Void> redirectToGoogle() {
        HttpHeaders headers = new HttpHeaders();
        var uri = loginGoogleService.gerarUrl();
        headers.setLocation(URI.create(uri));
        return new ResponseEntity<>(headers, HttpStatus.FOUND);
    }

    @GetMapping("/auth")
    public ResponseEntity<String> handleGoogleAuthCode(@RequestParam String code) {
        var email = loginGoogleService.getEmailByCode(code);
        return ResponseEntity.status(HttpStatus.OK).body(email);
    }

    @PostMapping("/auth")
    public ResponseEntity<String> handleGoogleIdToken(@RequestParam("id_token") String idToken) {
        var email = loginGoogleService.getEmail(idToken);
        return ResponseEntity.status(HttpStatus.OK).body(email);
    }
}
