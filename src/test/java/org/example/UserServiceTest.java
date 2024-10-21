package org.example;

import org.example.dto.ApiResponse;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.exceptions.InvalidUsernameAndPasswordException;
import org.example.models.User;
import org.example.repositories.UserRepository;
import org.example.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequest userRequest;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userRequest = new UserRequest("testUser", "testPassword");
        user = User.builder()
                .username(userRequest.getUsername())
                .password(userRequest.getPassword())
                .build();
    }

    @Test
    void testCreateUser() {
        when(userRepository.save(any(User.class))).thenReturn(user);

        ResponseEntity<ApiResponse> response = userService.createUser(userRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("User created successfully", response.getBody().getMessage());
        assertEquals(userRequest.getUsername(), ((UserResponse) response.getBody().getData().get("user")).getUsername());

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testCreateUser_NullUsername() {
        userRequest.setUsername(null);

        ResponseEntity<ApiResponse> response = userService.createUser(userRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username cannot be null or empty", response.getBody().getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_NullPassword() {
        userRequest.setPassword(null);

        ResponseEntity<ApiResponse> response = userService.createUser(userRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Password cannot be null or empty", response.getBody().getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserById_UserNotFound() {
        String userId = "1";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> userService.getUserById(userId));
        assertEquals("User not found", exception.getReason());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testCreateUser_EmptyUsername() {
        userRequest.setUsername("");

        ResponseEntity<ApiResponse> response = userService.createUser(userRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Username cannot be null or empty", response.getBody().getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testCreateUser_EmptyPassword() {
        userRequest.setPassword("");

        ResponseEntity<ApiResponse> response = userService.createUser(userRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Password cannot be null or empty", response.getBody().getMessage());

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testGetUserById_UserFound() {
        String userId = "1";

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        ResponseEntity<ApiResponse> response = userService.getUserById(userId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fetched successfully", response.getBody().getMessage());
        assertEquals(userRequest.getUsername(), ((UserResponse) response.getBody().getData().get("user")).getUsername());

        verify(userRepository, times(1)).findById(userId);
    }

    @Test
    void testLoginUser_Success() {
        when(userRepository.findByUsername(userRequest.getUsername())).thenReturn(Optional.of(user));

        ResponseEntity<ApiResponse> response = userService.loginUser(userRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Login successful", response.getBody().getMessage());
        assertEquals(userRequest.getUsername(), ((UserResponse) response.getBody().getData().get("user")).getUsername());

        verify(userRepository, times(1)).findByUsername(userRequest.getUsername());
    }

    @Test
    void testLoginUser_InvalidCredentials() {
        when(userRepository.findByUsername(userRequest.getUsername())).thenReturn(Optional.of(user));

        userRequest.setPassword("wrongPassword");

        InvalidUsernameAndPasswordException exception = assertThrows(InvalidUsernameAndPasswordException.class, () -> userService.loginUser(userRequest));
        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepository, times(1)).findByUsername(userRequest.getUsername());
    }

    @Test
    void testLoginUser_UserNotFound() {
        when(userRepository.findByUsername(userRequest.getUsername())).thenReturn(Optional.empty());

        InvalidUsernameAndPasswordException exception = assertThrows(InvalidUsernameAndPasswordException.class, () -> userService.loginUser(userRequest));
        assertEquals("Invalid username or password", exception.getMessage());

        verify(userRepository, times(1)).findByUsername(userRequest.getUsername());
    }
}
