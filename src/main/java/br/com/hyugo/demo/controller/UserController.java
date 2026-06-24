package br.com.hyugo.demo.controller;

import br.com.hyugo.demo.dto.response.UserResponse;
import br.com.hyugo.demo.repository.UserRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserRepository repository;

    public UserController(UserRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        List<UserResponse> users = repository.findAll(Sort.by(Sort.Direction.ASC, "id"))
                .stream()
                .map(UserResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(users);
    }
}
