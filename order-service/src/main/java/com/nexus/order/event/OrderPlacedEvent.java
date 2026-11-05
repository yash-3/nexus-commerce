package com.nexus.order.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public record OrderPlacedEvent(
    String orderNumber,
    String customerEmail,
    BigDecimal totalAmount,
    List<OrderItemEvent> items,
    Instant timestamp
) {}
