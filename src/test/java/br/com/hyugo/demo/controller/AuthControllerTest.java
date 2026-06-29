package br.com.hyugo.demo.controller;

import br.com.hyugo.demo.config.TokenConfig;
import br.com.hyugo.demo.dto.request.LoginRequest;
import br.com.hyugo.demo.dto.request.RegisterUserRequest;
import br.com.hyugo.demo.dto.response.ErrorResponse;
import br.com.hyugo.demo.dto.response.LoginResponse;
import br.com.hyugo.demo.entity.Role;
import br.com.hyugo.demo.entity.User;
import br.com.hyugo.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @Test
    void shouldReturnTokenAndRoleWhenCredentialsAreValid() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        TokenConfig tokenConfig = mock(TokenConfig.class);
        Authentication authentication = mock(Authentication.class);
        AuthController controller = new AuthController(repository, passwordEncoder, authenticationManager, tokenConfig);

        User user = new User();
        user.setEmail("admin@email.com");
        user.setRole(Role.ADMIN);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(tokenConfig.generateToken(user)).thenReturn("jwt-token");

        ResponseEntity<?> response = controller.login(new LoginRequest("admin@email.com", "senha123"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new LoginResponse("jwt-token", Role.ADMIN));
    }

    @Test
    void shouldReturnUnauthorizedWhenCredentialsAreInvalid() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        TokenConfig tokenConfig = mock(TokenConfig.class);
        AuthController controller = new AuthController(repository, passwordEncoder, authenticationManager, tokenConfig);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("bad credentials"));

        ResponseEntity<?> response = controller.login(new LoginRequest("hugo@email.com", "senha-invalida"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Email ou senha inválidos."));
    }

    @Test
    void shouldRejectRegisteringDuplicateEmail() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        TokenConfig tokenConfig = mock(TokenConfig.class);
        AuthController controller = new AuthController(repository, passwordEncoder, authenticationManager, tokenConfig);

        when(repository.existsByEmail("hugo@email.com")).thenReturn(true);

        ResponseEntity<?> response = controller.register(new RegisterUserRequest("Hugo", "hugo@email.com", "senha123", null), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Já existe um usuário com esse email."));
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void shouldCreateUserRoleWhenRoleIsNotProvided() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        TokenConfig tokenConfig = mock(TokenConfig.class);
        AuthController controller = new AuthController(repository, passwordEncoder, authenticationManager, tokenConfig);

        when(repository.existsByEmail("user@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senha-criptografada");

        ResponseEntity<?> response = controller.register(new RegisterUserRequest("User", "user@email.com", "senha123", null), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(new br.com.hyugo.demo.dto.response.RegisterUserResponse("User", "user@email.com", Role.USER));
    }

    @Test
    void shouldRejectAdminRegistrationWithoutAuthenticatedAdmin() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        TokenConfig tokenConfig = mock(TokenConfig.class);
        AuthController controller = new AuthController(repository, passwordEncoder, authenticationManager, tokenConfig);

        when(repository.existsByEmail("admin@email.com")).thenReturn(false);

        ResponseEntity<?> response = controller.register(new RegisterUserRequest("Admin", "admin@email.com", "senha123", Role.ADMIN), null);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(response.getBody()).isEqualTo(new ErrorResponse("Somente um admin pode cadastrar outro admin."));
        verify(repository, never()).save(any(User.class));
    }

    @Test
    void shouldAllowAdminRegistrationWhenRequesterIsAdmin() {
        UserRepository repository = mock(UserRepository.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        TokenConfig tokenConfig = mock(TokenConfig.class);
        AuthController controller = new AuthController(repository, passwordEncoder, authenticationManager, tokenConfig);
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin@email.com",
                null,
                java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        when(repository.existsByEmail("admin@email.com")).thenReturn(false);
        when(passwordEncoder.encode("senha123")).thenReturn("senha-criptografada");

        ResponseEntity<?> response = controller.register(
                new RegisterUserRequest("Admin", "admin@email.com", "senha123", Role.ADMIN),
                authentication
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isEqualTo(new br.com.hyugo.demo.dto.response.RegisterUserResponse("Admin", "admin@email.com", Role.ADMIN));
    }
}
