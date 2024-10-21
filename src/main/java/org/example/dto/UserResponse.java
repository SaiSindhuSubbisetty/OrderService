package org.example.dto;

import lombok.Data;
import org.example.models.User;

@Data
public class UserResponse {
    private String id;
    private String username;

    public UserResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
    }
}
