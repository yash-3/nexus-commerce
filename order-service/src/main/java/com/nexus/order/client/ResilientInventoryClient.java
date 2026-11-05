package com.nexus.order.client;

import com.nexus.order.dto.client.BatchStockCheckRequest;
import com.nexus.order.dto.client.BatchStockCheckResponse;
import com.nexus.order.exception.InventoryServiceUnavailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ResilientInventoryClient {

    private static final Logger log = LoggerFactory.getLogger(ResilientInventoryClient.class);

    private final InventoryClient inventoryClient;
    private final int maxRetries;
    private final long backoffMs;

    public ResilientInventoryClient(
            InventoryClient inventoryClient,
            @Value("${app.client.inventory.max-retries:3}") int maxRetries,
            @Value("${app.client.inventory.retry-backoff-ms:200}") long backoffMs) {
        this.inventoryClient = inventoryClient;
        this.maxRetries = maxRetries;
        this.backoffMs = backoffMs;
    }

    public BatchStockCheckResponse checkStockWithResilience(BatchStockCheckRequest request) {
        int attempt = 0;
        Exception lastException = null;

        while (attempt < maxRetries) {
            attempt++;
            try {
                log.info("Checking stock availability via InventoryClient (Attempt {}/{})", attempt, maxRetries);
                return inventoryClient.checkBatchAvailability(request);
            } catch (Exception ex) {
                lastException = ex;
                log.warn("Inventory service call failed on attempt {}/{}: {}", attempt, maxRetries, ex.getMessage());
                if (attempt < maxRetries) {
                    try {
                        Thread.sleep(backoffMs * attempt);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new InventoryServiceUnavailableException("Interrupted while retrying inventory service", ie);
                    }
                }
            }
        }

        throw new InventoryServiceUnavailableException(
                "Inventory service is currently unavailable after " + maxRetries + " attempts. Please retry shortly.",
                lastException
        );
    }
}
