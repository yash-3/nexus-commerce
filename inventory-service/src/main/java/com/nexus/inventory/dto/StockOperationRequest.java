package com.nexus.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record StockOperationRequest(
    @NotBlank(message = "SKU is mandatory")
    String sku,

    @NotNull(message = "Quantity is mandatory")
    @Positive(message = "Quantity must be strictly positive")
    Integer quantity
) {}
