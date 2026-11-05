package com.nexus.inventory.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record StockUpdateRequest(
    @NotNull(message = "Available quantity is mandatory")
    @PositiveOrZero(message = "Available quantity must be zero or positive")
    Integer availableQuantity,

    @NotNull(message = "Reorder threshold is mandatory")
    @PositiveOrZero(message = "Reorder threshold must be zero or positive")
    Integer reorderThreshold
) {}
