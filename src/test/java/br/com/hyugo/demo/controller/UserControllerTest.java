package br.com.hyugo.demo.controller;

import br.com.hyugo.demo.dto.response.UserResponse;
import br.com.hyugo.demo.entity.Role;
import br.com.hyugo.demo.entity.User;
import br.com.hyugo.demo.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
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
        firstUser.setRole(Role.ADMIN);

        User secondUser = new User();
        secondUser.setId(2L);
        secondUser.setName("Bruno");
        secondUser.setEmail("bruno@email.com");
        secondUser.setPassword("outra-senha-criptografada");
        secondUser.setRole(Role.USER);

        Sort sortById = Sort.by(Sort.Direction.ASC, "id");
        when(repository.findAll(sortById)).thenReturn(List.of(firstUser, secondUser));

        ResponseEntity<List<UserResponse>> response = controller.listUsers();

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsExactly(
                new UserResponse(1L, "Ana", "ana@email.com", Role.ADMIN),
                new UserResponse(2L, "Bruno", "bruno@email.com", Role.USER)
        );
        verify(repository).findAll(sortById);
    }

    @Test
    void shouldDeleteUserByIdWhenItExists() {
        UserRepository repository = mock(UserRepository.class);
        UserController controller = new UserController(repository);

        when(repository.existsById(1L)).thenReturn(true);

        ResponseEntity<Void> response = controller.deleteUserById(1L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        verify(repository).existsById(1L);
        verify(repository).deleteById(1L);
    }

    @Test
    void shouldReturnNotFoundWhenDeletingUnknownUser() {
        UserRepository repository = mock(UserRepository.class);
        UserController controller = new UserController(repository);

        when(repository.existsById(99L)).thenReturn(false);

        ResponseEntity<Void> response = controller.deleteUserById(99L);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        verify(repository).existsById(99L);
        verify(repository, never()).deleteById(99L);
    }
}
