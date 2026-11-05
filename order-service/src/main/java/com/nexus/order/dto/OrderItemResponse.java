package com.nexus.order.dto;

import java.math.BigDecimal;

public record OrderItemResponse(
    Long id,
    String sku,
    Integer quantity,
    BigDecimal unitPrice,
    BigDecimal subtotal
) {}
