package org.example.services;

import lombok.RequiredArgsConstructor;
import org.example.dto.ApiResponse;
import org.example.dto.UserRequest;
import org.example.dto.UserResponse;
import org.example.exceptions.InvalidUsernameAndPasswordException;
import org.example.models.User;
import org.example.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.Map;

import static org.example.constants.Constants.*;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .build();
    }

    public ResponseEntity<ApiResponse> createUser(UserRequest request) {
        try {
            validateUserCredentials(request.getUsername(), request.getPassword());

            User user = User.builder()
                    .username(request.getUsername())
                    .password(passwordEncoder.encode(request.getPassword()))  // Encode password before saving
                    .build();

            userRepository.save(user);

            ApiResponse response = ApiResponse.builder()
                    .message(USER_CREATED)
                    .status(HttpStatus.CREATED)
                    .data(Map.of("user", new UserResponse(user)))
                    .build();

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (InvalidUsernameAndPasswordException e) {
            ApiResponse response = ApiResponse.builder()
                    .message(e.getMessage())
                    .status(HttpStatus.BAD_REQUEST)
                    .build();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    public ResponseEntity<ApiResponse> getUserById(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        ApiResponse response = ApiResponse.builder()
                .message(FETCHED)
                .status(HttpStatus.OK)
                .data(Map.of("user", new UserResponse(user)))
                .build();

        return ResponseEntity.ok(response);
    }

    public ResponseEntity<ApiResponse> loginUser(UserRequest request) {
        validateUserCredentials(request.getUsername(), request.getPassword());

        User user = userRepository.findByUsername(request.getUsername())
                .filter(u -> passwordEncoder.matches(request.getPassword(), u.getPassword()))  // Check encoded password
                .orElseThrow(() -> new InvalidUsernameAndPasswordException("Invalid username or password"));

        ApiResponse response = ApiResponse.builder()
                .message(LOGIN_SUCCESS)
                .status(HttpStatus.OK)
                .data(Map.of("user", new UserResponse(user)))
                .build();

        return ResponseEntity.ok(response);
    }

    private void validateUserCredentials(String username, String password) {
        if (username == null || username.trim().isEmpty()) {
            throw new InvalidUsernameAndPasswordException("Username cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new InvalidUsernameAndPasswordException("Password cannot be null or empty");
        }
    }
}