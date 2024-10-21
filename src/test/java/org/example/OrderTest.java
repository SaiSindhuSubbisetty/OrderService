package org.example;


import org.example.models.Order;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void testOrderInitialization() {
        Order order = new Order();

        assertNull(order.getId());
        assertNull(order.getUserId());
        assertNull(order.getItems());
        assertNull(order.getTotalPrice());
        assertNull(order.getStatus());
    }

    @Test
    void testOrderCreation() {
        Order order = Order.builder()
                .id("order123")
                .userId("user123")
                .items(List.of("item1", "item2"))
                .totalPrice(100.0)
                .status("Pending")
                .build();

        assertEquals("order123", order.getId());
        assertEquals("user123", order.getUserId());
        assertEquals(List.of("item1", "item2"), order.getItems());
        assertEquals(100.0, order.getTotalPrice());
        assertEquals("Pending", order.getStatus());
    }

}