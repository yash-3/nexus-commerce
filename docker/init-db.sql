-- ==============================================================================
-- Nexus Commerce Database Provisioning Script
-- Target: PostgreSQL 16+
-- Microservice Bounded Context Isolation:
--   1. product_db   -> Product Catalog & Dynamic Pricing
--   2. inventory_db -> Stock Allocation, Reservoirs & Warehouse Tracking
--   3. order_db     -> Order Orchestration & Saga Coordination
--   4. payment_db   -> Payment Ledger, Escrow & Transaction Logs
-- ==============================================================================

-- Create microservice-isolated databases
SELECT 'CREATE DATABASE product_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'product_db')\gexec

SELECT 'CREATE DATABASE inventory_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'inventory_db')\gexec

SELECT 'CREATE DATABASE order_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'order_db')\gexec

SELECT 'CREATE DATABASE payment_db'
WHERE NOT EXISTS (SELECT FROM pg_database WHERE datname = 'payment_db')\gexec

-- Grant full schema permissions to postgres application user
GRANT ALL PRIVILEGES ON DATABASE product_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE inventory_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE order_db TO postgres;
GRANT ALL PRIVILEGES ON DATABASE payment_db TO postgres;
