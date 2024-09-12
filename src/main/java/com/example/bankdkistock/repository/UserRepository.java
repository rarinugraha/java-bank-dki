package com.example.bankdkistock.repository;

import com.example.bankdkistock.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository {
    Optional<User> findByUsername(String username);

    long count();

    void seedUser(String username, String password);
}

