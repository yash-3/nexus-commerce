package com.nexus.order.dto.client;

public record ItemAvailability(
    String sku,
    int requestedQuantity,
    int availableQuantity,
    boolean available
) {}
