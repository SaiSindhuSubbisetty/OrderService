package org.example;

import org.example.models.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserTest {

    @Test
    void testUserInitialization() {
        User user = new User();

        assertNull(user.getId());
        assertNull(user.getUsername());
        assertNull(user.getPassword());
    }

    @Test
    void testUserCreation() {
        User user = User.builder()
                .id("user123")
                .username("testUser")
                .password("securePassword")
                .build();

        assertEquals("user123", user.getId());
        assertEquals("testUser", user.getUsername());
        assertEquals("securePassword", user.getPassword());
    }

}
