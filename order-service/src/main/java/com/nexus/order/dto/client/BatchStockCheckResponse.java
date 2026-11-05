package com.nexus.order.dto.client;

import java.util.List;

public record BatchStockCheckResponse(
    boolean allInStock,
    List<ItemAvailability> items
) {}
