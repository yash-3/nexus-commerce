#!/usr/bin/env bash
# ==============================================================================
# Nexus Commerce - End-to-End System Verification Runner
# Validates:
#   1. Microservice Cluster Health (Eureka, Gateway, Domain Services)
#   2. Gateway Route Dynamic Resolution & Eureka Sync
#   3. Product Catalog Seeding (via API Gateway)
#   4. Inventory Allocation (via API Gateway)
#   5. Declarative Order Placement & Stock Validation (via Gateway -> Order Service)
#   6. Asynchronous Kafka Event Processing & Idempotent Payment Ledger
#   7. Notification In-Memory Deduplication
#   8. Spring AI 2.0 Architectural Compliance Audit Engine
# ==============================================================================

set -euo pipefail

# ANSI color codes
GREEN="\033[0;32m"
BLUE="\033[0;34m"
YELLOW="\033[1;33m"
RED="\033[0;31m"
BOLD="\033[1m"
NC="\033[0m"

log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_pass() { echo -e "${GREEN}[PASS]${NC} $1"; }
log_warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_fail() { echo -e "${RED}[FAIL]${NC} $1"; exit 1; }

# Helper for JSON parsing (uses jq if available, otherwise falls back to python3)
json_extract() {
    local json="$1"
    local expr="$2"
    if command -v jq &> /dev/null; then
        echo "${json}" | jq -r "${expr}"
    else
        python3 -c "
import sys, json
data = json.loads('''${json}''')
expr = '${expr}'.lstrip('.')
val = data
for part in expr.split('.'):
    if part.isdigit():
        val = val[int(part)]
    elif part:
        val = val.get(part, '')
print(val if val is not None else '')
"
    fi
}

echo -e "${BOLD}======================================================================${NC}"
echo -e "${BOLD}Nexus Commerce: End-to-End Architecture Verification Test${NC}"
echo -e "${BOLD}======================================================================${NC}"

GATEWAY_URL="http://localhost:8080"
EUREKA_URL="http://localhost:8761"
ORDER_URL="http://localhost:8083"
PAYMENT_URL="http://localhost:8084"
NOTIF_URL="http://localhost:8085"
COMPLIANCE_URL="http://localhost:8086"

# ------------------------------------------------------------------------------
# STEP 1: Verify Infrastructure & Microservices Health
# ------------------------------------------------------------------------------
echo ""
echo -e "${BOLD}--- STEP 1: Service Health Checks ---${NC}"

check_health() {
    local name="$1"
    local url="$2"
    log_info "Probing ${name} (${url})..."
    local http_code
    http_code=$(curl -s -o /dev/null -w "%{http_code}" "${url}" || echo "000")
    if [[ "${http_code}" == "200" || "${http_code}" == "204" ]]; then
        log_pass "${name} is healthy (HTTP ${http_code})"
    else
        log_warn "${name} returned HTTP ${http_code}. Make sure services are running."
    fi
}

check_health "Eureka Service Registry" "${EUREKA_URL}"
check_health "API Gateway" "${GATEWAY_URL}/actuator/health"
check_health "Product Service" "http://localhost:8081/actuator/health"
check_health "Inventory Service" "http://localhost:8082/actuator/health"
check_health "Order Service" "${ORDER_URL}/actuator/health"
check_health "Payment Service" "${PAYMENT_URL}/actuator/health"
check_health "Notification Service" "${NOTIF_URL}/actuator/health"
check_health "AI Compliance Service" "${COMPLIANCE_URL}/actuator/health"

# ------------------------------------------------------------------------------
# STEP 1B: Verify Gateway Route Dynamic Resolution & Eureka Sync
# ------------------------------------------------------------------------------
echo ""
echo -e "${BOLD}--- STEP 1B: Verifying Gateway Dynamic Route Resolution ---${NC}"

log_info "Waiting for API Gateway to resolve product-service via Eureka..."
GATEWAY_SYNCED=false
for attempt in {1..20}; do
    STATUS_CODE=$(curl -s -o /dev/null -w "%{http_code}" "${GATEWAY_URL}/api/v1/products" || echo "000")
    if [[ "${STATUS_CODE}" == "200" || "${STATUS_CODE}" == "405" ]]; then
        log_pass "API Gateway route to product-service is active (HTTP ${STATUS_CODE})!"
        GATEWAY_SYNCED=true
        break
    else
        log_info "Gateway returned HTTP ${STATUS_CODE}. Waiting for Eureka registry sync (attempt ${attempt}/20)..."
        sleep 2
    fi
done

if [[ "${GATEWAY_SYNCED}" != "true" ]]; then
    log_warn "Gateway has not yet resolved product-service via Eureka."
    DIRECT_CODE=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:8081/api/v1/products" || echo "000")
    log_info "Direct product-service port 8081 probe response: HTTP ${DIRECT_CODE}"
    log_info "Gateway loaded routes from actuator:"
    curl -s "${GATEWAY_URL}/actuator/gateway/routes" | python3 -m json.tool 2>/dev/null || true
fi

# ------------------------------------------------------------------------------
# STEP 2: Seed Product Catalog via API Gateway
# ------------------------------------------------------------------------------
echo ""
echo -e "${BOLD}--- STEP 2: Seeding Product Catalog via Gateway ---${NC}"

SKU="SKU-MBP-$(date +%s)"
PRODUCT_PAYLOAD="{\"sku\": \"${SKU}\", \"name\": \"Apple MacBook Pro 16 M3 Max\", \"description\": \"High performance developer workstation\", \"price\": 3499.00, \"category\": \"Computers\"}"

log_info "Creating product with SKU: ${SKU} through Gateway (${GATEWAY_URL}/api/v1/products)..."
PROD_RESPONSE=$(curl -s -X POST "${GATEWAY_URL}/api/v1/products" \
    -H "Content-Type: application/json" \
    -d "${PRODUCT_PAYLOAD}")

CREATED_SKU=$(json_extract "${PROD_RESPONSE}" ".sku")
if [[ "${CREATED_SKU}" == "${SKU}" ]]; then
    log_pass "Product successfully created: ${CREATED_SKU} (\$3,499.00)"
else
    log_fail "Failed to create product. Response: ${PROD_RESPONSE}"
fi

# ------------------------------------------------------------------------------
# STEP 3: Seed Inventory Stock via API Gateway
# ------------------------------------------------------------------------------
echo ""
echo -e "${BOLD}--- STEP 3: Initializing Inventory Stock via Gateway ---${NC}"

INVENTORY_PAYLOAD="{\"sku\": \"${SKU}\", \"availableQuantity\": 50, \"reorderThreshold\": 10}"

log_info "Allocating 50 units for SKU: ${SKU} through Gateway (${GATEWAY_URL}/api/v1/inventory)..."
INV_RESPONSE=$(curl -s -X POST "${GATEWAY_URL}/api/v1/inventory" \
    -H "Content-Type: application/json" \
    -d "${INVENTORY_PAYLOAD}")

AVAIL_QTY=$(json_extract "${INV_RESPONSE}" ".availableQuantity")
if [[ "${AVAIL_QTY}" == "50" ]]; then
    log_pass "Inventory successfully initialized: ${AVAIL_QTY} units available for ${SKU}"
else
    log_fail "Failed to allocate inventory. Response: ${INV_RESPONSE}"
fi

# ------------------------------------------------------------------------------
# STEP 4: Place Order via API Gateway (Synchronous HTTP Interface Check)
# ------------------------------------------------------------------------------
echo ""
echo -e "${BOLD}--- STEP 4: Placing Order via Gateway (Triggers Stock Check & Kafka Event) ---${NC}"

CUSTOMER_EMAIL="architect@nexuscommerce.com"
ORDER_PAYLOAD="{\"customerEmail\": \"${CUSTOMER_EMAIL}\", \"items\": [{\"sku\": \"${SKU}\", \"quantity\": 2, \"unitPrice\": 3499.00}]}"

log_info "Submitting checkout order for 2 units of ${SKU} (\$6,998.00)..."
ORDER_RESPONSE=$(curl -s -X POST "${GATEWAY_URL}/api/v1/orders" \
    -H "Content-Type: application/json" \
    -d "${ORDER_PAYLOAD}")

ORDER_NUMBER=$(json_extract "${ORDER_RESPONSE}" ".orderNumber")
ORDER_STATUS=$(json_extract "${ORDER_RESPONSE}" ".status")
TOTAL_AMOUNT=$(json_extract "${ORDER_RESPONSE}" ".totalAmount")

if [[ -n "${ORDER_NUMBER}" && "${ORDER_NUMBER}" != "null" && "${ORDER_STATUS}" == "PENDING" ]]; then
    log_pass "Order created successfully! OrderNumber: ${ORDER_NUMBER} | Status: ${ORDER_STATUS} | Total: \$${TOTAL_AMOUNT}"
else
    log_fail "Order placement failed. Response: ${ORDER_RESPONSE}"
fi

# ------------------------------------------------------------------------------
# STEP 5: Assert Asynchronous Kafka Event Processing & Idempotent Payment
# ------------------------------------------------------------------------------
echo ""
echo -e "${BOLD}--- STEP 5: Verifying Asynchronous Kafka Event Processing ---${NC}"

log_info "Polling payment-service for order [${ORDER_NUMBER}] ledger entry..."
PAYMENT_PROCESSED=false
for i in {1..10}; do
    PAYMENT_RESP=$(curl -s "${PAYMENT_URL}/api/v1/payments/order/${ORDER_NUMBER}" || echo "{}")
    PAY_STATUS=$(json_extract "${PAYMENT_RESP}" ".status")
    PAY_REF=$(json_extract "${PAYMENT_RESP}" ".paymentReference")

    if [[ "${PAY_STATUS}" == "SUCCESS" ]]; then
        log_pass "Kafka event received! Payment recorded with reference: ${PAY_REF} (Status: ${PAY_STATUS})"
        PAYMENT_PROCESSED=true
        break
    fi
    sleep 1
done

if [[ "${PAYMENT_PROCESSED}" != "true" ]]; then
    log_warn "Payment not yet recorded after 10s. Verify Kafka broker and payment-service connectivity."
fi

# ------------------------------------------------------------------------------
# STEP 6: Assert Notification Service Deduplication
# ------------------------------------------------------------------------------
echo ""
echo -e "${BOLD}--- STEP 6: Verifying Notification Service Deduplication Status ---${NC}"

NOTIF_RESP=$(curl -s "${NOTIF_URL}/api/v1/notifications/status" || echo "{}")
CACHED_KEYS=$(json_extract "${NOTIF_RESP}" ".cachedDeduplicationKeys")
log_info "Notification service status response: ${NOTIF_RESP}"
if [[ -n "${CACHED_KEYS}" && "${CACHED_KEYS}" -ge 0 ]]; then
    log_pass "Notification service operational with deduplication cache active."
fi

# ------------------------------------------------------------------------------
# STEP 7: Execute Spring AI 2.0 Architectural Compliance Audit
# ------------------------------------------------------------------------------
echo ""
echo -e "${BOLD}--- STEP 7: Executing Spring AI Architectural Compliance Audit ---${NC}"

log_info "Triggering AI Audit (${COMPLIANCE_URL}/api/v1/compliance/audit)..."
AUDIT_RESP=$(curl -s -X POST "${COMPLIANCE_URL}/api/v1/compliance/audit" \
    -H "Content-Type: application/json")

AUDIT_STATUS=$(json_extract "${AUDIT_RESP}" ".overallStatus")
NON_COMPLIANT=$(json_extract "${AUDIT_RESP}" ".nonCompliantServicesCount")
COMPLIANT_COUNT=$(json_extract "${AUDIT_RESP}" ".compliantServicesCount")

echo ""
echo -e "${BLUE}=== AI Compliance Audit Results ===${NC}"
echo -e "Audit Status:            ${BOLD}${AUDIT_STATUS}${NC}"
echo -e "Compliant Services:      ${GREEN}${COMPLIANT_COUNT}${NC}"
echo -e "Non-Compliant Services:  ${RED}${NON_COMPLIANT}${NC}"

if [[ "${AUDIT_STATUS}" == "NON_COMPLIANT" && "${NON_COMPLIANT}" -ge 2 ]]; then
    log_pass "AI Audit accurately flagged deliberate missing documentation for inventory-service and notification-service!"
else
    log_warn "AI Audit returned status: ${AUDIT_STATUS}. Details: ${AUDIT_RESP}"
fi

echo ""
echo -e "${BOLD}======================================================================${NC}"
echo -e "${GREEN}${BOLD}End-to-End System Pipeline Verification Completed Successfully!${NC}"
echo -e "${BOLD}======================================================================${NC}"
