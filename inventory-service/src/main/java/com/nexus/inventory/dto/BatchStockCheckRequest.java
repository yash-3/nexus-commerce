package com.nexus.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record BatchStockCheckRequest(
    @NotEmpty(message = "Items check list cannot be empty")
    List<@Valid SkuQuantityPair> items
) {}
