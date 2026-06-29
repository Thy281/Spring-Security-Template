package br.com.hyugo.demo.dto.response;

import br.com.hyugo.demo.entity.Role;

public record LoginResponse(String token, Role role) {
}
