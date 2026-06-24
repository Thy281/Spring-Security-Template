package br.com.hyugo.demo.controller;

import br.com.hyugo.demo.dto.response.UserResponse;
import br.com.hyugo.demo.entity.User;
import br.com.hyugo.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserControllerTest {

    @Test
    void shouldListRegisteredUsers() {
        UserRepository repository = mock(UserRepository.class);
        UserController controller = new UserController(repository);

        User firstUser = new User();
        firstUser.setId(1L);
        firstUser.setName("Ana");
        firstUser.setEmail("ana@email.com");
        firstUser.setPassword("senha-criptografada");

        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setName("Bruno");
        secondUser.setEmail("bruno@email.com");
        secondUser.setPassword("outra-senha-criptografada");

        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        when(repository.findAll(sortById)).thenReturn(List.of(firstUser, secondUser));

        ResponseEntity<List<UserResponse>> response = controller.listUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(
                new UserResponse(1L, "Ana", "ana@email.com"),
                new UserResponse(2L, "Bruno", "bruno@email.com")
        );
        verify(repository).findAll(sortById);
    }
}
