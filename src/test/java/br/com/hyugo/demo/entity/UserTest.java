package br.com.hyugo.demo.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void shouldUseEmailAsUsername() {
        User user = new User();
        user.setEmail("hugo@email.com");

        assertThat(user.getUsername()).isEqualTo("hugo@email.com");
    }

    @Test
    void shouldExposeRoleAsGrantedAuthority() {
        User user = new User();
        user.setRole(Role.ADMIN);

        assertThat(user.getAuthorities())
                .extracting(authority -> authority.getAuthority())
                .containsExactly("ROLE_ADMIN");
    }
}
