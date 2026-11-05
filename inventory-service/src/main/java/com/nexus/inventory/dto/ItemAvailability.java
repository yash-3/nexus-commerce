package com.nexus.inventory.dto;

public record ItemAvailability(
    String sku,
    int requestedQuantity,
    int availableQuantity,
    boolean available
) {}
