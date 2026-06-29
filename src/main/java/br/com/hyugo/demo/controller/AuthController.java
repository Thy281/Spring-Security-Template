package br.com.hyugo.demo.controller;

import br.com.hyugo.demo.config.TokenConfig;
import br.com.hyugo.demo.dto.request.LoginRequest;
import br.com.hyugo.demo.dto.request.RegisterUserRequest;
import br.com.hyugo.demo.dto.response.ErrorResponse;
import br.com.hyugo.demo.dto.response.LoginResponse;
import br.com.hyugo.demo.dto.response.RegisterUserResponse;
import br.com.hyugo.demo.entity.Role;
import br.com.hyugo.demo.entity.User;
import br.com.hyugo.demo.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenConfig tokenConfig;

    public AuthController(UserRepository repository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, TokenConfig tokenConfig) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.tokenConfig = tokenConfig;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            var authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.email(),
                            loginRequest.password())
            );
            User user = (User) authentication.getPrincipal();
            String token = tokenConfig.generateToken(user);
            return ResponseEntity.ok(new LoginResponse(token, user.getRole()));
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Email ou senha inválidos."));
        }
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterUserRequest request, Authentication authentication) {
        if (repository.existsByEmail(request.email())) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Já existe um usuário com esse email."));
        }

        Role requestedRole = request.role() == null ? Role.USER : request.role();
        if (requestedRole == Role.ADMIN && (authentication == null || authentication.getAuthorities().stream()
                .noneMatch(authority -> Role.ADMIN.authority().equals(authority.getAuthority())))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(new ErrorResponse("Somente um admin pode cadastrar outro admin."));
        }

        User newUser = new User();
        newUser.setName(request.name());
        newUser.setEmail(request.email());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRole(requestedRole);

        try {
            repository.save(newUser);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(new RegisterUserResponse(newUser.getName(), newUser.getEmail(), newUser.getRole()));
        } catch (DataIntegrityViolationException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(new ErrorResponse("Já existe um usuário com esse email."));
        }
    }
}
