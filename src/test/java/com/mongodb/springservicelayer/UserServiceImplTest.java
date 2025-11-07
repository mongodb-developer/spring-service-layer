package com.mongodb.springservicelayer;

import com.mongodb.springservicelayer.exception.DuplicateEmailException;
import com.mongodb.springservicelayer.exception.InvalidEmailException;
import com.mongodb.springservicelayer.exception.UserInactiveException;
import com.mongodb.springservicelayer.exception.UserNotFoundException;
import com.mongodb.springservicelayer.model.User;
import com.mongodb.springservicelayer.repository.UserRepository;
import com.mongodb.springservicelayer.service.EmailService;
import com.mongodb.springservicelayer.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserServiceImpl
 * Tests business logic in isolation with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("123", "john@example.com", "John Doe",
                LocalDateTime.now(), true);
    }

    // ===== createUser Tests =====

    @Test
    void createUser_ValidData_CreatesAndReturnsUser() {
        // Given
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendWelcomeEmail(any(User.class));

        // When
        User result = userService.createUser("john@example.com", "John Doe");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("john@example.com");
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.isActive()).isTrue();
        assertThat(result.getId()).isNotNull();
        assertThat(result.getCreatedAt()).isNotNull();

        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(userRepository, times(1)).save(any(User.class));
        verify(emailService, times(1)).sendWelcomeEmail(any(User.class));
    }

    @Test
    void createUser_DuplicateEmail_ThrowsDuplicateEmailException() {
        // Given
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.createUser("john@example.com", "John Doe"))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessageContaining("already exists");

        verify(userRepository, times(1)).existsByEmail("john@example.com");
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendWelcomeEmail(any(User.class));
    }

    @Test
    void createUser_InvalidEmailFormat_ThrowsInvalidEmailException() {
        // Given
        when(userRepository.existsByEmail("invalid-email")).thenReturn(false);

        // When & Then
        assertThatThrownBy(() -> userService.createUser("invalid-email", "John Doe"))
                .isInstanceOf(InvalidEmailException.class)
                .hasMessageContaining("Invalid email format");

        verify(userRepository, times(1)).existsByEmail("invalid-email");
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendWelcomeEmail(any(User.class));
    }

    @Test
    void createUser_NullEmail_ThrowsInvalidEmailException() {
        // When & Then
        assertThatThrownBy(() -> userService.createUser(null, "John Doe"))
                .isInstanceOf(InvalidEmailException.class);

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void createUser_SendsWelcomeEmail() {
        // Given
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendWelcomeEmail(any(User.class));

        // When
        userService.createUser("john@example.com", "John Doe");

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(emailService).sendWelcomeEmail(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getEmail()).isEqualTo("john@example.com");
        assertThat(capturedUser.getName()).isEqualTo("John Doe");
    }

    // ===== getUserById Tests =====

    @Test
    void getUserById_ExistingUser_ReturnsUser() {
        // Given
        when(userRepository.findById("123")).thenReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById("123");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo("123");
        assertThat(result.getEmail()).isEqualTo("john@example.com");

        verify(userRepository, times(1)).findById("123");
    }

    @Test
    void getUserById_NonExistentUser_ThrowsUserNotFoundException() {
        // Given
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById("999"))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("User not found with id: 999");

        verify(userRepository, times(1)).findById("999");
    }

    // ===== updateUserName Tests =====

    @Test
    void updateUserName_ActiveUser_UpdatesAndReturnsUser() {
        // Given
        when(userRepository.findById("123")).thenReturn(Optional.of(testUser));

        User updatedUser = new User("123", "john@example.com", "Jane Doe",
                LocalDateTime.now(), true);
        when(userRepository.save(any(User.class))).thenReturn(updatedUser);

        // When
        User result = userService.updateUserName("123", "Jane Doe");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Jane Doe");

        verify(userRepository, times(1)).findById("123");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUserName_InactiveUser_ThrowsUserInactiveException() {
        // Given
        User inactiveUser = new User("123", "john@example.com", "John Doe",
                LocalDateTime.now(), false);
        when(userRepository.findById("123")).thenReturn(Optional.of(inactiveUser));

        // When & Then
        assertThatThrownBy(() -> userService.updateUserName("123", "Jane Doe"))
                .isInstanceOf(UserInactiveException.class)
                .hasMessageContaining("Cannot update inactive user");

        verify(userRepository, times(1)).findById("123");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updateUserName_NonExistentUser_ThrowsUserNotFoundException() {
        // Given
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.updateUserName("999", "Jane Doe"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById("999");
        verify(userRepository, never()).save(any(User.class));
    }

    // ===== deactivateUser Tests =====

    @Test
    void deactivateUser_ExistingUser_DeactivatesAndSendsEmail() {
        // Given
        when(userRepository.findById("123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendDeactivationEmail(any(User.class));

        // When
        userService.deactivateUser("123");

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User deactivatedUser = userCaptor.getValue();
        assertThat(deactivatedUser.isActive()).isFalse();

        verify(userRepository, times(1)).findById("123");
        verify(emailService, times(1)).sendDeactivationEmail(any(User.class));
    }

    @Test
    void deactivateUser_NonExistentUser_ThrowsUserNotFoundException() {
        // Given
        when(userRepository.findById("999")).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.deactivateUser("999"))
                .isInstanceOf(UserNotFoundException.class);

        verify(userRepository, times(1)).findById("999");
        verify(userRepository, never()).save(any(User.class));
        verify(emailService, never()).sendDeactivationEmail(any(User.class));
    }

    @Test
    void deactivateUser_SendsDeactivationEmail() {
        // Given
        when(userRepository.findById("123")).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(emailService).sendDeactivationEmail(any(User.class));

        // When
        userService.deactivateUser("123");

        // Then
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(emailService).sendDeactivationEmail(userCaptor.capture());

        User capturedUser = userCaptor.getValue();
        assertThat(capturedUser.getId()).isEqualTo("123");
        assertThat(capturedUser.isActive()).isFalse();
    }

    // ===== getAllActiveUsers Tests =====

    @Test
    void getAllActiveUsers_MixedUsers_ReturnsOnlyActiveUsers() {
        // Given
        User activeUser1 = new User("1", "user1@example.com", "User One",
                LocalDateTime.now(), true);
        User inactiveUser = new User("2", "user2@example.com", "User Two",
                LocalDateTime.now(), false);
        User activeUser2 = new User("3", "user3@example.com", "User Three",
                LocalDateTime.now(), true);

        when(userRepository.findAll()).thenReturn(Arrays.asList(activeUser1, inactiveUser, activeUser2));

        // When
        List<User> result = userService.getAllActiveUsers();

        // Then
        assertThat(result).hasSize(2);
        assertThat(result).extracting(User::getId).containsExactly("1", "3");
        assertThat(result).allMatch(User::isActive);

        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllActiveUsers_NoUsers_ReturnsEmptyList() {
        // Given
        when(userRepository.findAll()).thenReturn(List.of());

        // When
        List<User> result = userService.getAllActiveUsers();

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllActiveUsers_AllInactive_ReturnsEmptyList() {
        // Given
        User inactiveUser1 = new User("1", "user1@example.com", "User One",
                LocalDateTime.now(), false);
        User inactiveUser2 = new User("2", "user2@example.com", "User Two",
                LocalDateTime.now(), false);

        when(userRepository.findAll()).thenReturn(Arrays.asList(inactiveUser1, inactiveUser2));

        // When
        List<User> result = userService.getAllActiveUsers();

        // Then
        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findAll();
    }
}