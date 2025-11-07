package com.mongodb.springservicelayer.service;

import com.mongodb.springservicelayer.model.User;

import java.util.List;

public interface UserService {
    User createUser(String email, String name);
    User getUserById(String id);
    User updateUserName(String id, String newName);
    void deactivateUser(String id);
    List<User> getAllActiveUsers();
}