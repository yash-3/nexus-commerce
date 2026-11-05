package com.nexus.product.dto;

import java.math.BigDecimal;

public record PriceCheckResponse(
    String sku,
    BigDecimal price,
    boolean availableForPurchase
) {}
