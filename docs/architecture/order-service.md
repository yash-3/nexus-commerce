# Order Service Architecture Specification

## 1. Domain Overview
The `order-service` coordinates the order lifecycle, executing distributed checkout sagas,
validating item availability via synchronous HTTP Interfaces, and initiating asynchronous event choreographies.

- **Port**: 8083
- **Bounded Context**: Order Orchestration Domain
- **Database**: PostgreSQL (`order_db`)
- **Key Invariants**:
  - Orders cannot be confirmed without real-time stock validation from `inventory-service`.
  - Every successfully persisted order emits an `OrderPlacedEvent` to Kafka.
  - Inter-service communication uses load-balanced Spring HTTP Interfaces (`@HttpExchange`).

## 2. Event Choreography
- **Produced Topics**:
  - `order-placed-topic` (Key: `orderNumber`, Value: `OrderPlacedEvent`)
