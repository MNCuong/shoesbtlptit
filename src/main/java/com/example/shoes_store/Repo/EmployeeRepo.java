package com.example.shoes_store.Repo;

import com.example.shoes_store.Entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepo extends JpaRepository<Employee, Long> {
    List<Employee> findByPosition(String position);

    Optional<Employee> findByUserId(Long userId);

    boolean existsByCode(String code);

    Optional<Employee> findByCode(String code);
}
