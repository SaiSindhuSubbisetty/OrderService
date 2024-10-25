package org.example;

import org.example.dto.ApiResponse;
import org.example.dto.ItemResponse;
import org.example.dto.OrderRequest;
import org.example.dto.OrderResponse;
import org.example.exceptions.InternalServerErrorException;
import org.example.exceptions.OrderIsMisplacedException;
import org.example.exceptions.OrderNotFoundException;
import org.example.models.Order;
import org.example.repositories.CatalogClient;
import org.example.repositories.OrderRepository;
import org.example.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import proto.Fulfillment;
import proto.FulfillmentServiceGrpc;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private CatalogClient catalogClient;

    @Mock
    private FulfillmentServiceGrpc.FulfillmentServiceBlockingStub fulfillmentServiceBlockingStub;

    @InjectMocks
    private OrderService orderService;

    private OrderRequest orderRequest;
    private Order order;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        orderRequest = new OrderRequest("userId", List.of("item1", "item2"), 100.0);
        order = Order.builder()
                .id("orderId")
                .userId("userId")
                .items(List.of("item1", "item2"))
                .totalPrice(100.0)
                .status("Pending")
                .build();
    }

    @Test
    void testCreateOrder_Success() {
        OrderRequest validRequest = new OrderRequest("userId", Collections.singletonList("item1"), 100.0);

        when(catalogClient.getItemById(anyString())).thenReturn(new ItemResponse("item1", "Item 1", "Description", "restaurantId", 100.0));

        Fulfillment.AssignOrderResponse response = Fulfillment.AssignOrderResponse.newBuilder().setStatus("ASSIGNED").build();
        when(fulfillmentServiceBlockingStub.assignOrder(any())).thenReturn(response);

        // ResponseEntity<ApiResponse> result = orderService.createOrder(validRequest);

        // Assert successful order creation
        //assertEquals(HttpStatus.CREATED, result.getStatusCode());
    }


    @Test
    void testCreateOrder_NullRequest_ShouldThrowIllegalArgumentException() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(null);
        });

        assertEquals("Invalid order request", exception.getMessage());
    }

    @Test
    void testCreateOrder_EmptyItems_ShouldThrowIllegalArgumentException() {
        orderRequest.setItems(Collections.emptyList());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(orderRequest);
        });

        assertEquals("Invalid order request", exception.getMessage());
    }

    @Test
    void testCreateOrder_NullUserId_ShouldThrowIllegalArgumentException() {
        orderRequest.setUserId(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            orderService.createOrder(orderRequest);
        });

        assertEquals("Invalid order request", exception.getMessage());
    }

    @Test
    void testCreateOrder_ItemNotFound_ShouldThrowInternalServerErrorException() {
        when(catalogClient.getItemById("item1")).thenReturn(null);

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class, () -> {
            orderService.createOrder(orderRequest);
        });

        assertEquals("Item not found: item1", exception.getMessage());
    }

    @Test
    void testGetOrderById_Success() {
        when(orderRepository.findById("orderId")).thenReturn(Optional.of(order));
        Fulfillment.GetOrderStatusResponse getOrderStatusResponse = Fulfillment.GetOrderStatusResponse.newBuilder().setStatus("Pending").build();
        when(fulfillmentServiceBlockingStub.getOrderStatus(any(Fulfillment.GetOrderStatusRequest.class))).thenReturn(getOrderStatusResponse);

        ResponseEntity<ApiResponse> response = orderService.getOrderById("orderId");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Fetched successfully", response.getBody().getMessage());
        assertTrue(response.getBody().getData().containsKey("order"));
        verify(orderRepository).findById("orderId");
    }

    @Test
    void testGetOrderById_OrderNotFound() {
        when(orderRepository.findById("invalidOrderId")).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> orderService.getOrderById("invalidOrderId"));
        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository).findById("invalidOrderId");
    }

    @Test
    void testGetOrderById_FulfillmentServiceFailure() {
        when(orderRepository.findById("orderId")).thenReturn(Optional.of(order));
        when(fulfillmentServiceBlockingStub.getOrderStatus(any(Fulfillment.GetOrderStatusRequest.class)))
                .thenThrow(new RuntimeException("Fulfillment service failure"));

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> orderService.getOrderById("orderId"));
        assertEquals("Fulfillment service failure", exception.getMessage());

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
        assertNotNull(response.getBody());
        assertEquals("Fetched successfully", response.getBody().getMessage());
        assertTrue(response.getBody().getData().containsKey("orders"));
        assertEquals(1, ((List<OrderResponse>) response.getBody().getData().get("orders")).size());

        verify(orderRepository).findAllByUserId("userId");
    }

    @Test
    void testGetOrdersByUserId_NoOrdersFound() {
        when(orderRepository.findAllByUserId("userId")).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse> response = orderService.getOrdersByUserId("userId");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Fetched successfully", response.getBody().getMessage());
        assertTrue(response.getBody().getData().containsKey("orders"));
        assertEquals(0, ((List<OrderResponse>) response.getBody().getData().get("orders")).size());

        verify(orderRepository).findAllByUserId("userId");
    }

    @Test
    void testGetAllOrders_Success() {
        when(orderRepository.findAll()).thenReturn(Collections.singletonList(order));

        ResponseEntity<ApiResponse> response = orderService.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Fetched successfully", response.getBody().getMessage());
        assertTrue(response.getBody().getData().containsKey("orders"));
        assertEquals(1, ((List<OrderResponse>) response.getBody().getData().get("orders")).size());

        verify(orderRepository).findAll();
    }

    @Test
    void testGetAllOrders_NoOrdersFound() {
        when(orderRepository.findAll()).thenReturn(Collections.emptyList());

        ResponseEntity<ApiResponse> response = orderService.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Fetched successfully", response.getBody().getMessage());
        assertTrue(response.getBody().getData().containsKey("orders"));
        assertEquals(0, ((List<OrderResponse>) response.getBody().getData().get("orders")).size());

        verify(orderRepository).findAll();
    }

    @Test
    void testDeleteOrder_Success() {
        when(orderRepository.findById("orderId")).thenReturn(Optional.of(order));

        ResponseEntity<ApiResponse> response = orderService.deleteOrder("orderId");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order deleted successfully", response.getBody().getMessage());

        verify(orderRepository).findById("orderId");
        verify(orderRepository).delete(order);
    }

    @Test
    void testDeleteOrder_OrderNotFound() {
        when(orderRepository.findById("invalidOrderId")).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> orderService.deleteOrder("invalidOrderId"));
        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository).findById("invalidOrderId");
        verify(orderRepository, never()).delete(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_Success() {
        when(orderRepository.findById("orderId")).thenReturn(Optional.of(order));
        Fulfillment.UpdateOrderStatusResponse updateOrderStatusResponse = Fulfillment.UpdateOrderStatusResponse.newBuilder().setStatus("UPDATED").build();
        when(fulfillmentServiceBlockingStub.updateOrderStatus(any(Fulfillment.UpdateOrderStatusRequest.class))).thenReturn(updateOrderStatusResponse);

        ResponseEntity<ApiResponse> response = orderService.updateOrderStatus("orderId", "Delivered");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Order status updated successfully", response.getBody().getMessage());
        assertTrue(response.getBody().getData().containsKey("order"));
        verify(orderRepository).findById("orderId");
        verify(orderRepository).save(order);
    }

    @Test
    void testUpdateOrderStatus_OrderNotFound() {
        when(orderRepository.findById("invalidOrderId")).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class,
                () -> orderService.updateOrderStatus("invalidOrderId", "Delivered"));
        assertEquals("Order not found", exception.getMessage());

        verify(orderRepository).findById("invalidOrderId");
    }

    @Test
    void testUpdateOrderStatus_OrderIsMisplaced() {
        when(orderRepository.findById("orderId")).thenReturn(Optional.of(order));

        OrderIsMisplacedException exception = assertThrows(OrderIsMisplacedException.class,
                () -> orderService.updateOrderStatus("orderId", "Misplaced"));
        assertEquals("The order has been marked as misplaced.", exception.getMessage());

        verify(orderRepository).findById("orderId");
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_FulfillmentServiceFailure() {
        when(orderRepository.findById("orderId")).thenReturn(Optional.of(order));
        Fulfillment.UpdateOrderStatusResponse updateOrderStatusResponse = Fulfillment.UpdateOrderStatusResponse.newBuilder().setStatus("FAILED").build();
        when(fulfillmentServiceBlockingStub.updateOrderStatus(any(Fulfillment.UpdateOrderStatusRequest.class))).thenReturn(updateOrderStatusResponse);

        InternalServerErrorException exception = assertThrows(InternalServerErrorException.class,
                () -> orderService.updateOrderStatus("orderId", "Delivered"));
        assertEquals("Failed to update order status.", exception.getMessage());

        verify(orderRepository).findById("orderId");
        verify(orderRepository, never()).save(any(Order.class));
    }
}