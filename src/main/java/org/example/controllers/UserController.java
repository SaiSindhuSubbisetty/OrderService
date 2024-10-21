package org.example.controllers;

import lombok.RequiredArgsConstructor;
import org.example.dto.ApiResponse;
import org.example.dto.UserRequest;
import org.example.exceptions.InvalidUsernameAndPasswordException;
import org.example.services.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse> createUser(@RequestBody UserRequest request) {
        if (request.getUsername() == null || request.getUsername().isEmpty() || request.getPassword() == null || request.getPassword().isEmpty()) {
            throw new InvalidUsernameAndPasswordException("Invalid credentials");
        }
        ApiResponse response = userService.createUser(request).getBody();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse> getUserById(@PathVariable String userId) {
        return userService.getUserById(userId);
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse> loginUser(@RequestBody UserRequest request) {
        return userService.loginUser(request);
    }
}