package br.com.hyugo.demo.repository;

import br.com.hyugo.demo.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findFirstByEmailOrderByIdDesc(String email);

    boolean existsByEmail(String email);
}
