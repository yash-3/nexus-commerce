package com.nexus.payment.service;

import com.nexus.payment.dto.PaymentResponse;
import com.nexus.payment.entity.PaymentStatus;
import com.nexus.payment.entity.PaymentTransaction;
import com.nexus.payment.event.OrderPlacedEvent;
import com.nexus.payment.event.PaymentCompletedEvent;
import com.nexus.payment.exception.PaymentNotFoundException;
import com.nexus.payment.publisher.PaymentEventPublisher;
import com.nexus.payment.repository.PaymentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final PaymentEventPublisher paymentEventPublisher;

    public PaymentService(PaymentRepository paymentRepository, PaymentEventPublisher paymentEventPublisher) {
        this.paymentRepository = paymentRepository;
        this.paymentEventPublisher = paymentEventPublisher;
    }

    /**
     * Idempotent payment processing for OrderPlacedEvent.
     * Prevents duplicate charging by verifying existing successful transactions for orderNumber.
     */
    @Transactional
    public PaymentTransaction processOrderPayment(OrderPlacedEvent event) {
        log.info("Processing payment for OrderPlacedEvent: orderNumber={}, amount={}, customer={}",
                event.orderNumber(), event.totalAmount(), event.customerEmail());

        // Step 1: Idempotency Check
        Optional<PaymentTransaction> existingTx = paymentRepository.findByOrderNumber(event.orderNumber());
        if (existingTx.isPresent()) {
            PaymentTransaction tx = existingTx.get();
            if (tx.getStatus() == PaymentStatus.SUCCESS) {
                log.warn("Idempotency guard triggered: Order {} already successfully charged with reference {}. Skipping duplicate payment.",
                        event.orderNumber(), tx.getPaymentReference());
                return tx;
            }
        }

        // Step 2: Simulated Payment Gateway Execution
        String paymentRef = "PAY-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        boolean paymentApproved = executeGatewayCharge(event.customerEmail(), event.totalAmount());

        PaymentTransaction transaction = new PaymentTransaction(
                paymentRef,
                event.orderNumber(),
                event.customerEmail(),
                event.totalAmount(),
                paymentApproved ? PaymentStatus.SUCCESS : PaymentStatus.FAILED,
                paymentApproved ? null : "Simulated bank authorization decline"
        );

        PaymentTransaction savedTx = paymentRepository.save(transaction);
        log.info("Payment transaction recorded: reference={}, status={}", savedTx.getPaymentReference(), savedTx.getStatus());

        // Step 3: Publish PaymentCompletedEvent
        PaymentCompletedEvent completedEvent = new PaymentCompletedEvent(
                savedTx.getPaymentReference(),
                savedTx.getOrderNumber(),
                savedTx.getCustomerEmail(),
                savedTx.getAmount(),
                savedTx.getStatus(),
                Instant.now()
        );

        paymentEventPublisher.publishPaymentCompleted(completedEvent);
        return savedTx;
    }

    public PaymentResponse getPaymentByReference(String reference) {
        return paymentRepository.findByPaymentReference(reference)
                .map(this::mapToResponse)
                .orElseThrow(() -> new PaymentNotFoundException("Payment reference '" + reference + "' not found"));
    }

    public PaymentResponse getPaymentByOrderNumber(String orderNumber) {
        return paymentRepository.findByOrderNumber(orderNumber)
                .map(this::mapToResponse)
                .orElseThrow(() -> new PaymentNotFoundException("Payment for order '" + orderNumber + "' not found"));
    }

    public List<PaymentResponse> getAllPayments(String customerEmail) {
        List<PaymentTransaction> txs = (customerEmail != null && !customerEmail.isBlank())
                ? paymentRepository.findByCustomerEmailIgnoreCase(customerEmail.trim())
                : paymentRepository.findAll();

        return txs.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private boolean executeGatewayCharge(String email, BigDecimal amount) {
        // Production hook for Stripe / Adyen / PayPal integration
        // For architectural demo, amounts > 0 are approved
        return amount != null && amount.compareTo(BigDecimal.ZERO) > 0;
    }

    private PaymentResponse mapToResponse(PaymentTransaction tx) {
        return new PaymentResponse(
                tx.getId(),
                tx.getPaymentReference(),
                tx.getOrderNumber(),
                tx.getCustomerEmail(),
                tx.getAmount(),
                tx.getStatus(),
                tx.getFailureReason(),
                tx.getCreatedAt(),
                tx.getUpdatedAt()
        );
    }
}
