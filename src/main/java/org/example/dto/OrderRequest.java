package org.example.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {
    private String userId;
    private String restaurantId;
    private String itemId;
    private List<String> items;
    private Double totalPrice;

    public OrderRequest(String userId, List<String> items, double totalPrice) {
        this.userId = userId;
        this.items = items;
        this.totalPrice = totalPrice;
    }

    public OrderRequest(String userId, String restaurantId, String itemId, double totalPrice) {
        this.userId = userId;
        this.restaurantId = restaurantId;
        this.itemId = itemId;
        this.totalPrice = totalPrice;
    }
}
