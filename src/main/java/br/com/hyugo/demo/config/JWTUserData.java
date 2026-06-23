package br.com.hyugo.demo.config;

import lombok.Builder;

@Builder
public record JWTUserData(Long userId, String email) {
}
