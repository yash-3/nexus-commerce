# Payment Service Architecture Specification

## 1. Domain Overview
The `payment-service` guarantees idempotent ledger accounting and gateway transaction execution.
It consumes order placement events and emits payment completion events.

- **Port**: 8084
- **Bounded Context**: Payment & Financial Ledger Domain
- **Database**: PostgreSQL (`payment_db`)
- **Key Invariants**:
  - Strict idempotency: Duplicate `OrderPlacedEvent` messages with existing `SUCCESS` transactions are skipped.
  - Successful charges emit `PaymentCompletedEvent` to `payment-completed-topic`.

## 2. Event Choreography
- **Consumed Topics**: `order-placed-topic`
- **Produced Topics**: `payment-completed-topic`
