package org.example.controllers;

import lombok.RequiredArgsConstructor;
import org.example.dto.ApiResponse;
import org.example.dto.OrderRequest;
import org.example.services.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse> createOrder(@RequestBody OrderRequest orderRequest) {
        return orderService.createOrder(orderRequest);
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse> getOrderById(@PathVariable String orderId) {
        return orderService.getOrderById(orderId);
    }

    @GetMapping
    public ResponseEntity<ApiResponse> getAllOrders() {
        return orderService.getAllOrders();
    }

    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse> updateOrderStatus(@PathVariable String orderId,
                                                         @RequestParam String status) {
        return orderService.updateOrderStatus(orderId, status);
    }

    @GetMapping("/test-fulfillment")
    public ResponseEntity<ApiResponse> testFulfillment(@RequestParam String orderId) {
        return orderService.getOrderById(orderId);
    }
}

