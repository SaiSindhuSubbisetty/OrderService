package org.example.services;

import jakarta.ws.rs.InternalServerErrorException;
import lombok.RequiredArgsConstructor;
import org.example.dto.ApiResponse;
import org.example.dto.OrderRequest;
import org.example.dto.OrderResponse;
import org.example.exceptions.OrderIsMisplacedException;
import org.example.exceptions.OrderNotFoundException;
import org.example.models.Order;
import org.example.repositories.OrderRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.example.constants.Constants.*;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    public ResponseEntity<ApiResponse> createOrder(OrderRequest request) {
        try {
            Order order = Order.builder()
                    .userId(request.getUserId())
                    .items(request.getItems())
                    .totalPrice(request.getTotalPrice())
                    .status("Pending")
                    .build();
            orderRepository.save(order);
            ApiResponse response = ApiResponse.builder()
                    .message(ORDER_CREATED)
                    .status(HttpStatus.CREATED)
                    .data(Map.of("order", new OrderResponse(order)))
                    .build();
            return ResponseEntity.status(response.getStatus()).body(response);
        } catch (Exception e) {
            throw new InternalServerErrorException("Failed to create order: " + e.getMessage());
        }
    }

    public ResponseEntity<ApiResponse> getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        ApiResponse response = ApiResponse.builder()
                .message(FETCHED)
                .status(HttpStatus.OK)
                .data(Map.of("order", new OrderResponse(order)))
                .build();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    public ResponseEntity<ApiResponse> getOrdersByUserId(String userId) {
        List<Order> orders = orderRepository.findAllByUserId(userId);
        List<OrderResponse> responses = new ArrayList<>();
        for (Order order : orders) {
            responses.add(new OrderResponse(order));
        }
        ApiResponse response = ApiResponse.builder()
                .message(FETCHED)
                .status(HttpStatus.OK)
                .data(Map.of("orders", responses))
                .build();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    public ResponseEntity<ApiResponse> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderResponse> responses = new ArrayList<>();
        for (Order order : orders) {
            responses.add(new OrderResponse(order));
        }
        ApiResponse response = ApiResponse.builder()
                .message(FETCHED)
                .status(HttpStatus.OK)
                .data(Map.of("orders", responses))
                .build();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    public ResponseEntity<ApiResponse> deleteOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        orderRepository.delete(order);
        ApiResponse response = ApiResponse.builder()
                .message(ORDER_DELETED)
                .status(HttpStatus.OK)
                .build();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    public ResponseEntity<ApiResponse> updateOrderStatus(String orderId, String status) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));

        if (status.equals("Misplaced")) {
            throw new OrderIsMisplacedException("The order has been marked as misplaced.");
        }

        order.setStatus(status);
        orderRepository.save(order);
        ApiResponse response = ApiResponse.builder()
                .message(ORDER_UPDATED)
                .status(HttpStatus.OK)
                .data(Map.of("order", new OrderResponse(order)))
                .build();
        return ResponseEntity.status(response.getStatus()).body(response);
    }
}