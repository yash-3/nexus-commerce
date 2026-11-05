# Nexus Commerce — Production-Grade Microservices Architecture

Platform stack:
- **Java 21 LTS** with Virtual Threads and Language Records
- **Spring Boot 4.0.0** and **Spring Framework 7.0.0**
- **Spring Cloud 2025.1.0** (Eureka Server/Client, Spring Cloud Gateway, Spring Cloud LoadBalancer)
- **Spring HTTP Interfaces** (`@HttpExchange`, `@PostExchange`) backed by load-balanced `RestClient` (Zero OpenFeign)
- **Jakarta EE 11 Validation**, **Hibernate 7.x**, **Jackson 3**
- **Apache Kafka** (KRaft mode, ZooKeeper-less)
- **Spring AI 2.0.2** for Dynamic Architecture & Documentation Compliance Auditing
- **PostgreSQL 16** with schema-isolated microservice databases

---

## Service Port Matrix

| Service | Port | Database | Primary Responsibility |
| :--- | :--- | :--- | :--- |
| `eureka-server` | `8761` | None | Service Discovery Registry |
| `api-gateway` | `8080` | None | Edge Routing & Global CORS |
| `product-service` | `8081` | `product_db` | Product Catalog & Dynamic Pricing |
| `inventory-service` | `8082` | `inventory_db` | Real-time Stock Allocation & Pessimistic Locks |
| `order-service` | `8083` | `order_db` | Saga Orchestration & HTTP Interfaces |
| `payment-service` | `8084` | `payment_db` | Idempotent Payment Ledger & Kafka Producer |
| `notification-service` | `8085` | None | Event Deduplication & Mock Dispatcher |
| `ai-compliance-service` | `8086` | None | Spring AI 2.0 Architectural Compliance Auditor |
| `kafka-ui` | `8088` | None | Web UI for Topic Observability |
| `kafka` | `9092, 29092` | None | Apache Kafka KRaft Event Broker |
| `postgres` | `5432` | Multiple | PostgreSQL 16 Multi-Tenant DB Server |

---

## Getting Started

### 1. Start Backing Infrastructure
```bash
cd nexus-commerce
docker compose up -d
```

### 2. Build Multi-Module Maven Project
```bash
./mvnw clean compile
# or: mvn clean compile
```

### 3. Launch Core Services
Start the microservices in the following recommended order:
1. `eureka-server`
2. `api-gateway`
3. `product-service`, `inventory-service`, `order-service`
4. `payment-service`, `notification-service`
5. `ai-compliance-service`

### 4. Execute End-to-End Verification Pipeline
Once services are running:
```bash
./verify-system.sh
```
