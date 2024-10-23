package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.controllers.UserController;
import org.example.dto.ApiResponse;
import org.example.dto.UserRequest;
import org.example.exceptions.GlobalExceptionHandler;
import org.example.exceptions.InvalidUsernameAndPasswordException;
import org.example.models.Order;
import org.example.models.User;
import org.example.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserControllerTest {

    private MockMvc mockMvc;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(userController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testCreateUser_Success() throws Exception {
        UserRequest userRequest = new UserRequest("testUser", "testPass");
        User user = new User();
        user.setId("1234");
        user.setUsername("testUser");
        user.setPassword("testPass");

        when(userService.createUser(any(UserRequest.class))).thenReturn(ResponseEntity.ok(ApiResponse.builder()
                .message("User created successfully")
                .status(HttpStatus.CREATED)
                .data(Map.of("user", user))
                .build()));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated());

        verify(userService, times(1)).createUser(any(UserRequest.class));
    }

    @Test
    void testCreateUser_InvalidUsernameOrPassword() throws Exception {
        UserRequest userRequest = new UserRequest("", "testPass");

        when(userService.createUser(any(UserRequest.class)))
                .thenThrow(new InvalidUsernameAndPasswordException("Invalid credentials"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).createUser(any(UserRequest.class));

    }


    @Test
    void testGetUserById_Success() throws Exception {
        String userId = "1234";
        User user = new User();
        user.setId(userId);
        user.setUsername("testUser");
        user.setPassword("testPass");

        when(userService.getUserById(userId)).thenReturn(ResponseEntity.ok(ApiResponse.builder()
                .message("User fetched successfully")
                .status(HttpStatus.OK)
                .data(Map.of("user", user))
                .build()));

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isOk());

        verify(userService, times(1)).getUserById(userId);
    }

    @Test
    void testGetUserById_NotFound() throws Exception {
        String userId = "9999";
        when(userService.getUserById(userId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/users/{userId}", userId))
                .andExpect(status().isNotFound());

        verify(userService, never()).createUser(any(UserRequest.class));

    }

    @Test
    void testLoginUser_Success() throws Exception {
        UserRequest userRequest = new UserRequest("testUser", "testPass");
        User user = new User();
        user.setId("1234");
        user.setUsername("testUser");
        user.setPassword("testPass");

        when(userService.loginUser(any(UserRequest.class))).thenReturn(ResponseEntity.ok(ApiResponse.builder()
                .message("Login successful")
                .status(HttpStatus.OK)
                .data(Map.of("user", user))
                .build()));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk());

        verify(userService, times(1)).loginUser(any(UserRequest.class));
    }

    @Test
    void testGetOrdersByUserId_Success() throws Exception {
        String userId = "user123";
        List<Order> orders = List.of(new Order(), new Order());
        ApiResponse response = ApiResponse.builder()
                .message("Fetched")
                .status(HttpStatus.OK)
                .data(Map.of("orders", orders))
                .build();

        when(userService.getOrdersByUserId(userId)).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/users/{userId}/orders/{orderId}", userId, "order123"))
                .andExpect(status().isOk());

        verify(userService, times(1)).getOrdersByUserId(userId);
    }

    @Test
    void testGetOrdersByUserId_UserNotFound() throws Exception {
        String userId = "user123";

        when(userService.getOrdersByUserId(userId)).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(get("/users/{userId}/orders/{orderId}", userId, "order123"))
                .andExpect(status().isNotFound());

        verify(userService, times(1)).getOrdersByUserId(userId);
    }

    @Test
    void testCreateUser_InvalidUsernameOrPassword3() throws Exception {
        String invalidUserJson = "{ \"username\": \"\", \"password\": \"password123\" }";

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidUserJson))
                .andExpect(status().isUnauthorized())
                .andExpect(result -> assertTrue(result.getResolvedException() instanceof InvalidUsernameAndPasswordException))
                .andExpect(result -> assertEquals("Invalid credentials", result.getResolvedException().getMessage()));

        verify(userService, never()).createUser(any(UserRequest.class));
    }

    @Test
    void testCreateUser_NullUsername() throws Exception {
        UserRequest userRequest = new UserRequest(null, "testPass");

        when(userService.createUser(any(UserRequest.class))).thenThrow(new InvalidUsernameAndPasswordException("Invalid credentials"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).createUser(any(UserRequest.class));

    }

    @Test
    void testCreateUser_NullPassword() throws Exception {
        UserRequest userRequest = new UserRequest("testUser", null);

        when(userService.createUser(any(UserRequest.class))).thenThrow(new InvalidUsernameAndPasswordException("Invalid credentials"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).createUser(any(UserRequest.class));

    }

    @Test
    void testCreateUser_EmptyUsername() throws Exception {
        UserRequest userRequest = new UserRequest("", "testPass");

        when(userService.createUser(any(UserRequest.class))).thenThrow(new InvalidUsernameAndPasswordException("Invalid credentials"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).createUser(any(UserRequest.class));

    }

    @Test
    void testCreateUser_EmptyPassword() throws Exception {
        UserRequest userRequest = new UserRequest("testUser", "");

        when(userService.createUser(any(UserRequest.class))).thenThrow(new InvalidUsernameAndPasswordException("Invalid credentials"));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isUnauthorized());

        verify(userService, never()).createUser(any(UserRequest.class));

    }

    @Test
    void testLoginUser_UserNotFound() throws Exception {
        UserRequest userRequest = new UserRequest("testUser", "testPass");

        when(userService.loginUser(any(UserRequest.class))).thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        mockMvc.perform(post("/users/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isNotFound());

        verify(userService, never()).createUser(any(UserRequest.class));

    }
}