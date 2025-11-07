package com.mongodb.springservicelayer.service;

import com.mongodb.springservicelayer.model.User;

public interface EmailService {
    void sendWelcomeEmail(User savedUser);
    void sendDeactivationEmail(User user);
}
