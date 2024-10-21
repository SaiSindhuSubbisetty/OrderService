package org.example.dto;

import lombok.Data;
import org.example.models.Order;

import java.util.List;

@Data
public class OrderResponse {
    private String id;
    private String userId;
    private List<String> items;
    private Double totalPrice;
    private String status;

    public OrderResponse(Order order) {
        this.id = order.getId();
        this.userId = order.getUserId();
        this.items = order.getItems();
        this.totalPrice = order.getTotalPrice();
        this.status = order.getStatus();
    }
}