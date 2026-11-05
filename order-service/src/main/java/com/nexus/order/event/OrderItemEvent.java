package com.nexus.order.event;

import java.math.BigDecimal;

public record OrderItemEvent(
    String sku,
    Integer quantity,
    BigDecimal unitPrice
) {}
