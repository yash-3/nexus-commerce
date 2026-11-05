package com.nexus.order.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record OrderItemRequest(
    @NotBlank(message = "Product SKU is mandatory")
    String sku,

    @NotNull(message = "Quantity is mandatory")
    @Positive(message = "Quantity must be strictly positive")
    Integer quantity,

    @NotNull(message = "Unit price is mandatory")
    @Positive(message = "Unit price must be strictly positive")
    BigDecimal unitPrice
) {}
