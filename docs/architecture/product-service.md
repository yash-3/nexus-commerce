# Product Service Architecture Specification

## 1. Domain Overview
The `product-service` manages the master catalog of items available in the Nexus Commerce platform.
It acts as the single source of truth for item pricing, categorization, SKU generation, and availability metadata.

- **Port**: 8081
- **Bounded Context**: Catalog & Pricing Domain
- **Database**: PostgreSQL (`product_db`)
- **Key Invariants**:
  - SKU codes are globally unique and immutable once created.
  - Prices must be strictly positive with two decimal places of precision.
  - Soft-deletion via `active=false` prevents orphaned historical references.

## 2. API Contract Summary
- `POST /api/v1/products`: Create catalog item
- `GET /api/v1/products/{id}`: Fetch product by primary key
- `GET /api/v1/products/sku/{sku}`: Query by SKU
- `GET /api/v1/products/price/{sku}`: Lightweight price check endpoint for checkout
