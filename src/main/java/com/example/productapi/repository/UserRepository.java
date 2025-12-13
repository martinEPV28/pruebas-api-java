package com.example.productapi.repository;

import com.example.productapi.model.User;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username);
}
