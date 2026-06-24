package br.com.hyugo.demo.dto.response;

import br.com.hyugo.demo.entity.User;

public record UserResponse(Long id, String name, String email) {

    public static UserResponse fromEntity(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
