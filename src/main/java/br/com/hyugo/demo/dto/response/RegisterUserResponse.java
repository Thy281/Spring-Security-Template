package br.com.hyugo.demo.dto.response;

import br.com.hyugo.demo.entity.Role;

public record RegisterUserResponse(String name, String email, Role role) {
}
