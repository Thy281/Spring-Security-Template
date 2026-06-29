package br.com.hyugo.demo.dto.request;

import br.com.hyugo.demo.entity.Role;
import jakarta.validation.constraints.NotBlank;

public record RegisterUserRequest(@NotBlank(message = "Name é obrigatório") String name,
                                  @NotBlank(message = "Email é obrigatório") String email,
                                  @NotBlank(message = "Password é obrigatório") String password,
                                  Role role) {
}
