package com.example.shoes_store.Service;

import com.example.shoes_store.Entity.User;
import com.example.shoes_store.dto.ChangePasswordRequest;

import java.util.List;
import java.util.Optional;


public interface UserService {

    Optional<User> getByEmail(String username);

    void changePassword(ChangePasswordRequest request, User user);

    User login(String username, String password);

    List<User> getAllUsers();

    List<User> getAllUsersByRole(String role);

    User getUserById(Long id);


    User registerUser(User user);

    boolean userExists(String username);
    boolean emailExists(String email);

    void updateUser(User updatedUser);

    void save(User user);

}

