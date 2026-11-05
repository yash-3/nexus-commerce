package com.nexus.order.client;

import com.nexus.order.dto.client.BatchStockCheckRequest;
import com.nexus.order.dto.client.BatchStockCheckResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

/**
 * Declarative Spring HTTP Interface for Inventory Service communication.
 * Backed by Spring Cloud LoadBalancer and RestClient (No OpenFeign).
 */
@HttpExchange("/api/v1/inventory")
public interface InventoryClient {

    @PostExchange("/check")
    BatchStockCheckResponse checkBatchAvailability(@RequestBody BatchStockCheckRequest request);
}
