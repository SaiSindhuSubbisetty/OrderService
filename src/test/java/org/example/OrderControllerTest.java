package org.example;

import org.example.controllers.OrderController;
import org.example.dto.ApiResponse;
import org.example.dto.OrderRequest;
import org.example.services.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import(SecurityConfigTest.class)
public class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private OrderService orderService;

    @Autowired
    private ObjectMapper objectMapper;

    private OrderRequest orderRequest;

    @BeforeEach
    void setUp() {
        orderRequest = new OrderRequest();
        orderRequest.setUserId("user123");
        orderRequest.setItems(List.of("item1", "item2"));
        orderRequest.setTotalPrice(100.0);
    }

    @Test
    void testCreateOrder() throws Exception {
        ApiResponse response = ApiResponse.builder()
                .message("Order created")
                .status(HttpStatus.CREATED)
                .data(Map.of("order", orderRequest))
                .build();
        when(orderService.createOrder(any(OrderRequest.class))).thenReturn(ResponseEntity.status(HttpStatus.CREATED).body(response));

        mockMvc.perform(post("/orders")
                        .with(user("admin").roles("ADMIN"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("Order created"));

        verify(orderService,times(1)).createOrder(any(OrderRequest.class));
    }

    @Test
    void testGetOrderById() throws Exception {
        ApiResponse response = ApiResponse.builder()
                .message("Fetched")
                .status(HttpStatus.OK)
                .data(Map.of("order", orderRequest))
                .build();
        when(orderService.getOrderById("order123")).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/orders/order123")
                        .with(user("user").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Fetched"));

        verify(orderService,times(1)).getOrderById("order123");
    }

    @Test
    void testGetAllOrders() throws Exception {
        ApiResponse response = ApiResponse.builder()
                .message("Fetched")
                .status(HttpStatus.OK)
                .data(Map.of("orders", List.of(orderRequest)))
                .build();
        when(orderService.getAllOrders()).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(get("/orders")
                        .with(user("admin").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Fetched"));

        verify(orderService,times(1)).getAllOrders();
    }

    @Test
    void testUpdateOrderStatus() throws Exception {
        ApiResponse response = ApiResponse.builder()
                .message("Order updated")
                .status(HttpStatus.OK)
                .data(Map.of("order", orderRequest))
                .build();
        when(orderService.updateOrderStatus("order123", "Shipped")).thenReturn(ResponseEntity.ok(response));

        mockMvc.perform(put("/orders/order123")
                        .with(user("admin").roles("ADMIN"))
                        .param("status", "Shipped"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Order updated"));

        verify(orderService,times(1)).updateOrderStatus("order123", "Shipped");
    }
}