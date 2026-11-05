package com.nexus.payment.dto;

import com.nexus.payment.entity.PaymentStatus;
import java.math.BigDecimal;
import java.time.Instant;

public record PaymentResponse(
    Long id,
    String paymentReference,
    String orderNumber,
    String customerEmail,
    BigDecimal amount,
    PaymentStatus status,
    String failureReason,
    Instant createdAt,
    Instant updatedAt
) {}
