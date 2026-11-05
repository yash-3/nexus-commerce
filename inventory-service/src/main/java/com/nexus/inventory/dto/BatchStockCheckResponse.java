package com.nexus.inventory.dto;

import java.util.List;

public record BatchStockCheckResponse(
    boolean allInStock,
    List<ItemAvailability> items
) {}
