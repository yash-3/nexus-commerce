package com.nexus.payment.event;

import com.nexus.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentCompletedEvent(
    String paymentReference,
    String orderNumber,
    String customerEmail,
    BigDecimal amount,
    PaymentStatus status,
    Instant timestamp
) {}
