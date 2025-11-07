package com.mongodb.springservicelayer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.springservicelayer.controller.CreateUserRequest;
import com.mongodb.springservicelayer.controller.UpdateNameRequest;
import com.mongodb.springservicelayer.controller.UserController;
import com.mongodb.springservicelayer.exception.DuplicateEmailException;
import com.mongodb.springservicelayer.exception.InvalidEmailException;
import com.mongodb.springservicelayer.exception.UserInactiveException;
import com.mongodb.springservicelayer.exception.UserNotFoundException;
import com.mongodb.springservicelayer.model.User;
import com.mongodb.springservicelayer.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for UserController
 * Tests the REST API endpoints without starting the full application
 */
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    void createUser_ValidRequest_ReturnsCreated() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com");
        User createdUser = new User("123", "john@example.com", "John Doe",
                LocalDateTime.now(), true);

        when(userService.createUser(anyString(), anyString())).thenReturn(createdUser);

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.active").value(true));

        verify(userService, times(1)).createUser("john@example.com", "John Doe");
    }

    @Test
    void createUser_DuplicateEmail_ReturnsConflict() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com");

        when(userService.createUser(anyString(), anyString()))
                .thenThrow(new DuplicateEmailException("User with email john@example.com already exists"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("User with email john@example.com already exists"));
    }

    @Test
    void createUser_InvalidEmail_ReturnsBadRequest() throws Exception {
        // Given
        CreateUserRequest request = new CreateUserRequest("John Doe", "john@example.com");

        when(userService.createUser(anyString(), anyString()))
                .thenThrow(new InvalidEmailException("Invalid email format: john@example.com"));

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Invalid email format: john@example.com"));
    }

    @Test
    void createUser_MissingName_ReturnsBadRequest() throws Exception {
        // Given - name is blank
        CreateUserRequest request = new CreateUserRequest("", "john@example.com");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(userService, never()).createUser(anyString(), anyString());
    }

    @Test
    void createUser_InvalidEmailFormat_ReturnsBadRequest() throws Exception {
        // Given - email doesn't match @Email pattern
        CreateUserRequest request = new CreateUserRequest("John Doe", "not-an-email");

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Validation Failed"));

        verify(userService, never()).createUser(anyString(), anyString());
    }

    @Test
    void getUser_ExistingUser_ReturnsOk() throws Exception {
        // Given
        User user = new User("123", "john@example.com", "John Doe",
                LocalDateTime.now(), true);

        when(userService.getUserById("123")).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/api/users/123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(userService, times(1)).getUserById("123");
    }

    @Test
    void getUser_NonExistentUser_ReturnsNotFound() throws Exception {
        // Given
        when(userService.getUserById("999"))
                .thenThrow(new UserNotFoundException("User not found with id: 999"));

        // When & Then
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));
    }

    @Test
    void updateName_ValidRequest_ReturnsOk() throws Exception {
        // Given
        UpdateNameRequest request = new UpdateNameRequest("Jane Doe");
        User updatedUser = new User("123", "john@example.com", "Jane Doe",
                LocalDateTime.now(), true);

        when(userService.updateUserName("123", "Jane Doe")).thenReturn(updatedUser);

        // When & Then
        mockMvc.perform(put("/api/users/123/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("123"))
                .andExpect(jsonPath("$.name").value("Jane Doe"));

        verify(userService, times(1)).updateUserName("123", "Jane Doe");
    }

    @Test
    void updateName_InactiveUser_ReturnsForbidden() throws Exception {
        // Given
        UpdateNameRequest request = new UpdateNameRequest("Jane Doe");

        when(userService.updateUserName("123", "Jane Doe"))
                .thenThrow(new UserInactiveException("Cannot update inactive user"));

        // When & Then
        mockMvc.perform(put("/api/users/123/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.message").value("Cannot update inactive user"));
    }

    @Test
    void updateName_BlankName_ReturnsBadRequest() throws Exception {
        // Given
        UpdateNameRequest request = new UpdateNameRequest("");

        // When & Then
        mockMvc.perform(put("/api/users/123/name")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).updateUserName(anyString(), anyString());
    }

    @Test
    void deactivateUser_ExistingUser_ReturnsNoContent() throws Exception {
        // Given
        doNothing().when(userService).deactivateUser("123");

        // When & Then
        mockMvc.perform(delete("/api/users/123"))
                .andExpect(status().isNoContent());

        verify(userService, times(1)).deactivateUser("123");
    }

    @Test
    void deactivateUser_NonExistentUser_ReturnsNotFound() throws Exception {
        // Given
        doThrow(new UserNotFoundException("User not found with id: 999"))
                .when(userService).deactivateUser("999");

        // When & Then
        mockMvc.perform(delete("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found with id: 999"));
    }

    @Test
    void getActiveUsers_ReturnsListOfActiveUsers() throws Exception {
        // Given
        List<User> activeUsers = Arrays.asList(
                new User("1", "user1@example.com", "User One", LocalDateTime.now(), true),
                new User("2", "user2@example.com", "User Two", LocalDateTime.now(), true)
        );

        when(userService.getAllActiveUsers()).thenReturn(activeUsers);

        // When & Then
        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].name").value("User One"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].name").value("User Two"));

        verify(userService, times(1)).getAllActiveUsers();
    }

    @Test
    void getActiveUsers_EmptyList_ReturnsEmptyArray() throws Exception {
        // Given
        when(userService.getAllActiveUsers()).thenReturn(List.of());

        // When & Then
        mockMvc.perform(get("/api/users/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}