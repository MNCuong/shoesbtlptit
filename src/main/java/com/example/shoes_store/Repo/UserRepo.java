package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    User findByUsername(String username);

    boolean existsByUsername(String username);

    List<User> findAllByRole(String role);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt BETWEEN :start AND :end")
    Integer countByCreatedAtBetween(@Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end);

    List<Object> findUserById(Long id);

    User findUserByUsername(String username);
}
