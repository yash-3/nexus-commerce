package com.nexus.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record ProductUpdateRequest(
    @NotBlank(message = "Product name is mandatory")
    String name,

    String description,

    @NotNull(message = "Price is mandatory")
    @Positive(message = "Price must be strictly positive")
    BigDecimal price,

    @NotBlank(message = "Category is mandatory")
    String category,

    boolean active
) {}
