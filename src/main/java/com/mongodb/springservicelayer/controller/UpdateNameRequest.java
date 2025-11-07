package com.mongodb.springservicelayer.controller;

import jakarta.validation.constraints.NotBlank;

/**
 * Request DTO for updating a user's name
 */
public record UpdateNameRequest(
        @NotBlank(message = "Name is required")
        String name
) {}