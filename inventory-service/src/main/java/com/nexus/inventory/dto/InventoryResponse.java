package com.nexus.inventory.dto;

import java.time.Instant;

public record InventoryResponse(
    Long id,
    String sku,
    Integer availableQuantity,
    Integer reservedQuantity,
    Integer totalQuantity,
    Integer reorderThreshold,
    boolean inStock,
    Instant createdAt,
    Instant updatedAt
) {}
