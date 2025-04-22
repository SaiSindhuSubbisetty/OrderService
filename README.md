# 🛒 Order Service

The **Order Service** is a core backend component in a microservice-based food delivery system. It handles the creation, retrieval, and management of user orders and interacts with the **Fulfillment Service** via gRPC for delivery operations.

---

## 🚀 Features

- User registration and login
- Order creation and management
- Order status updates
- Integration with Fulfillment Service via gRPC
- RESTful API using Spring Boot
- Data persistence with JPA and PostgreSQL

---

## 📁 Project Structure

```
.
├── controllers/       # REST controllers (Order & User)
├── dto/               # Request and response DTOs
├── exceptions/        # Custom exception classes
├── models/            # JPA entities and gRPC config
├── services/          # Business logic
├── proto/             # gRPC proto files
└── application.yml    # Spring Boot configuration
```

---

## 🧱 Data Models

### 🧾 Order

```java
@Entity
@Table(name = "orders")
public class Order {
    private String id;
    private String userId;
    private List<String> items;
    private Double totalPrice;
    private String status;
}
```

### 👤 User

```java
@Entity
@Table(name = "users")
public class User {
    private String id;
    private String username;
    private String password;
}
```

---

## 🌐 REST APIs

### 🛍️ Orders

| Method | Endpoint              | Description              |
|--------|------------------------|--------------------------|
| POST   | `/orders`              | Create a new order       |
| GET    | `/orders/{orderId}`    | Get order by ID          |
| GET    | `/orders`              | Get all orders           |
| PUT    | `/orders/{orderId}`    | Update order status      |
| GET    | `/orders/test-fulfillment?orderId={id}` | Test gRPC call to Fulfillment Service |

### 👥 Users

| Method | Endpoint                           | Description               |
|--------|------------------------------------|---------------------------|
| POST   | `/users`                           | Register a new user       |
| POST   | `/users/login`                     | Login with credentials    |
| GET    | `/users/{userId}`                  | Get user by ID            |
| GET    | `/users/{userId}/orders/{orderId}` | Get orders for a user     |

---

## 🛰️ gRPC Integration

This service uses gRPC to communicate with the **Fulfillment Service** for:

- Assigning orders to delivery persons
- Getting and updating order status
- Fetching orders for a delivery person

### Proto file location:
```proto
proto/fulfillment.proto
```

### gRPC Client Configuration:
```java
@Bean
public ManagedChannel managedChannel() {
    return ManagedChannelBuilder.forAddress("localhost", 50051).usePlaintext().build();
}

@Bean
public FulfillmentServiceGrpc.FulfillmentServiceBlockingStub fulfillmentServiceBlockingStub(ManagedChannel managedChannel) {
    return FulfillmentServiceGrpc.newBlockingStub(managedChannel);
}
```

---

## ⚙️ Tech Stack

- Java 17
- Spring Boot
- Spring Data JPA
- PostgreSQL
- gRPC (Java + Proto3)
- Lombok

---

## 🛠️ Running Locally

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/order-service.git
cd order-service
```

### 2. Configure Database

Update `application.yml` with your PostgreSQL credentials:
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/orderservice
    username: postgres
    password: yourpassword
```

### 3. Run the Service
```bash
./mvnw spring-boot:run
```

Service runs on: `http://localhost:8080`

---

## 🧪 Sample Request

```http
POST /orders
Content-Type: application/json

{
  "userId": "1234",
  "items": ["Burger", "Fries"],
  "totalPrice": 250.0,
  "status": "PLACED"
}
```

---

Swagger Doc :http://localhost:8082/swagger-ui/index.html

## 📄 License

This project is licensed under the MIT License.
