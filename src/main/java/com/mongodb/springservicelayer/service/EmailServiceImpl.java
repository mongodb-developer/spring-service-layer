package com.mongodb.springservicelayer.service;

import com.mongodb.springservicelayer.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendWelcomeEmail(User user) {
        logger.info("   Sending welcome email to: {} ({})", user.getName(), user.getEmail());
        logger.info("   Subject: Welcome to our platform!");
        logger.info("   Body: Hi {}, welcome aboard! Your account has been created.", user.getName());

    }

    @Override
    public void sendDeactivationEmail(User user) {
        logger.info("   Sending deactivation email to: {} ({})", user.getName(), user.getEmail());
        logger.info("   Subject: Account Deactivated");
        logger.info("   Body: Hi {}, your account has been deactivated. Contact support to reactivate.", user.getName());
    }
}