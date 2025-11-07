package com.mongodb.springservicelayer.service;

import com.mongodb.springservicelayer.exception.DuplicateEmailException;
import com.mongodb.springservicelayer.exception.InvalidEmailException;
import com.mongodb.springservicelayer.exception.UserInactiveException;
import com.mongodb.springservicelayer.exception.UserNotFoundException;
import com.mongodb.springservicelayer.model.User;
import com.mongodb.springservicelayer.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final EmailService emailService;

    public UserServiceImpl(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Override
    public User createUser(String email, String name) {  // No throws clause needed!
        // Business rule: email must be unique
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException("User with email " + email + " already exists");
        }

        // Business rule: validate email format
        if (!isValidEmail(email)) {
            throw new InvalidEmailException("Invalid email format: " + email);
        }

        // Create and save the user
        User user = new User();
        user.setId(generateId());
        user.setEmail(email);
        user.setName(name);
        user.setCreatedAt(LocalDateTime.now());
        user.setActive(true);

        User savedUser = userRepository.save(user);

        // Business operation: send welcome email
        emailService.sendWelcomeEmail(savedUser);

        return savedUser;
    }

    @Override
    public User getUserById(String id) {  // No throws clause needed!
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }

    @Override
    public User updateUserName(String id, String newName) {  // No throws clause needed!
        User user = getUserById(id);

        // Business rule: can't update deactivated users
        if (!user.isActive()) {
            throw new UserInactiveException("Cannot update inactive user");
        }

        user.setName(newName);
        return userRepository.save(user);
    }

    @Override
    public void deactivateUser(String id) {  // No throws clause needed!
        User user = getUserById(id);
        user.setActive(false);
        userRepository.save(user);

        // Business operation: send goodbye email
        emailService.sendDeactivationEmail(user);
    }

    @Override
    public List<User> getAllActiveUsers() {
        return userRepository.findAll().stream()
                .filter(User::isActive)
                .collect(Collectors.toList());
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private String generateId() {
        return UUID.randomUUID().toString();
    }
}