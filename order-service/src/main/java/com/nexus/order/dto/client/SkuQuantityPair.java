package com.nexus.order.dto.client;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SkuQuantityPair(
    @NotBlank(message = "SKU cannot be blank") String sku,
    @NotNull(message = "Quantity cannot be null") @Positive(message = "Quantity must be positive") Integer quantity
) {}
