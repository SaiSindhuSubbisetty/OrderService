package org.example.services;

import lombok.RequiredArgsConstructor;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import proto.Fulfillment;
import proto.FulfillmentServiceGrpc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.example.constants.Constants.*;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;
    private final FulfillmentServiceGrpc.FulfillmentServiceBlockingStub fulfillmentServiceBlockingStub;

    public ResponseEntity<ApiResponse> createOrder(OrderRequest request) {
        if (request == null || request.getItems() == null || request.getItems().isEmpty() || request.getUserId() == null) {
            throw new IllegalArgumentException("Invalid order request");
        }
        List<ItemResponse> itemResponses = new ArrayList<>();
        for (String itemId : request.getItems()) {
            ItemResponse itemResponse = catalogClient.getItemById(itemId);
            if (itemResponse == null) {
                throw new InternalServerErrorException("Item not found: " + itemId);
            }
            itemResponses.add(itemResponse);
        }
        double totalPrice = itemResponses.stream().mapToDouble(ItemResponse::getPrice).sum();
        Order order = Order.builder()
                .userId(request.getUserId())
                .items(request.getItems())
                .totalPrice(totalPrice)
                .status("Pending")
                .build();
        orderRepository.save(order);
        // Call the FulfillmentService to assign the order
        Fulfillment.AssignOrderRequest assignOrderRequest = Fulfillment.AssignOrderRequest.newBuilder()
                .setOrderId(order.getId())
                .setDeliveryPersonId("some-delivery-person-id") // You might want to determine this dynamically
                .build();
        Fulfillment.AssignOrderResponse assignOrderResponse = fulfillmentServiceBlockingStub.assignOrder(assignOrderRequest);
        if (!"ASSIGNED".equals(assignOrderResponse.getStatus())) {
            throw new InternalServerErrorException("Failed to assign order to a delivery person.");
        }
        ApiResponse response = ApiResponse.builder()
                .message(ORDER_CREATED)
                .status(HttpStatus.CREATED)
                .data(Map.of("order", new OrderResponse(order)))
                .build();
        return ResponseEntity.status(response.getStatus()).body(response);
    }

    public ResponseEntity<ApiResponse> getOrderById(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException("Order not found"));
        // Call the FulfillmentService to get the order status
        Fulfillment.GetOrderStatusRequest getOrderStatusRequest = Fulfillment.GetOrderStatusRequest.newBuilder()
                .setOrderId(orderId)
                .build();
        Fulfillment.GetOrderStatusResponse getOrderStatusResponse = fulfillmentServiceBlockingStub.getOrderStatus(getOrderStatusRequest);
        order.setStatus(getOrderStatusResponse.getStatus());
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
        // Call the FulfillmentService to update the order status
        Fulfillment.UpdateOrderStatusRequest updateOrderStatusRequest = Fulfillment.UpdateOrderStatusRequest.newBuilder()
                .setOrderId(orderId)
                .setStatus(status)
                .build();
        Fulfillment.UpdateOrderStatusResponse updateOrderStatusResponse = fulfillmentServiceBlockingStub.updateOrderStatus(updateOrderStatusRequest);
        if (!"UPDATED".equals(updateOrderStatusResponse.getStatus())) {
            throw new InternalServerErrorException("Failed to update order status.");
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