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
}
