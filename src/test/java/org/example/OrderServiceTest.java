package org.example;

import jakarta.ws.rs.InternalServerErrorException;
import org.example.dto.ApiResponse;
import org.example.dto.OrderRequest;
import org.example.dto.OrderResponse;
import org.example.exceptions.OrderIsMisplacedException;
import org.example.exceptions.OrderNotFoundException;
import org.example.models.Order;
import org.example.repositories.OrderRepository;
import org.example.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest orderRequest;
    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderRequest = new OrderRequest("userId", Collections.emptyList(), 100.0);
        order = Order.builder()
                .id("orderId")
                .userId("userId")
                .items(Collections.emptyList())
                .totalPrice(100.0)
                .status("Pending")
                .build();
    }

    @Test
    void testCreateOrder() {
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        ResponseEntity<ApiResponse> response = orderService.createOrder(orderRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("Order created successfully", response.getBody().getMessage());
        assertNotNull(response.getBody().getData().get("order"));

        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testGetOrderById_Success() {
        when(orderRepository.findById("orderId")).thenReturn(Optional.of(order));

        ResponseEntity<ApiResponse> response = orderService.getOrderById("orderId");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fetched successfully", response.getBody().getMessage());
        verify(orderRepository).findById("orderId");
    }

    @Test
    void testGetOrderById_NotFound() {
        when(orderRepository.findById("invalidOrderId")).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById("invalidOrderId"));
        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository).findById("invalidOrderId");
    }

    @Test
    void testGetOrdersByUserId_Success() {
        when(orderRepository.findAllByUserId("userId")).thenReturn(Collections.singletonList(order));

        ResponseEntity<ApiResponse> response = orderService.getOrdersByUserId("userId");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fetched successfully", response.getBody().getMessage());
        assertEquals(1, ((List<OrderResponse>) response.getBody().getData().get("orders")).size());

        verify(orderRepository).findAllByUserId("userId");
    }

    @Test
    void testGetAllOrders_Success() {
        when(orderRepository.findAll()).thenReturn(Collections.singletonList(order));

        ResponseEntity<ApiResponse> response = orderService.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Fetched successfully", response.getBody().getMessage());
        assertEquals(1, ((List<OrderResponse>) response.getBody().getData().get("orders")).size());

        verify(orderRepository).findAll();
    }

    @Test
    void testDeleteOrder_Success() {
        when(orderRepository.findById("orderId")).thenReturn(Optional.of(order));

        ResponseEntity<ApiResponse> response = orderService.deleteOrder("orderId");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order deleted successfully", response.getBody().getMessage());
        verify(orderRepository).delete(order);
    }

    @Test
    void testDeleteOrder_NotFound() {
        when(orderRepository.findById("invalidOrderId")).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> orderService.deleteOrder("invalidOrderId"));

        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository).findById("invalidOrderId");
    }

    @Test
    void testUpdateOrderStatus_Success() {
        when(orderRepository.findById("orderId")).thenReturn(Optional.of(order));

        ResponseEntity<ApiResponse> response = orderService.updateOrderStatus("orderId", "Shipped");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Order status updated successfully", response.getBody().getMessage());
        assertEquals("Shipped", order.getStatus());

        verify(orderRepository).save(order);
    }

    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        when(orderRepository.findById("invalidOrderId")).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> orderService.updateOrderStatus("invalidOrderId", "Shipped"));

        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository).findById("invalidOrderId");
    }

    @Test
    void testUpdateOrderStatus_OrderIsMisplaced() {
        when(orderRepository.findById("orderId")).thenReturn(Optional.of(order));

        OrderIsMisplacedException exception = assertThrows(OrderIsMisplacedException.class,
                () -> orderService.updateOrderStatus("orderId", "Misplaced"));

        assertEquals("The order has been marked as misplaced.", exception.getMessage());

        verify(orderRepository, never()).save(any(Order.class));
    }
}
