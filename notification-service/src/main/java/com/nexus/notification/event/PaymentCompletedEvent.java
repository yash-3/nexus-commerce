package com.nexus.notification.event;

import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(
    String paymentReference,
    String orderNumber,
    String customerEmail,
    BigDecimal amount,
    String status,
    Instant timestamp
) {}
