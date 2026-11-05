package com.nexus.payment.controller;

import com.nexus.payment.dto.PaymentResponse;
import com.nexus.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/{reference}")
    public ResponseEntity<PaymentResponse> getPaymentByReference(@PathVariable String reference) {
        return ResponseEntity.ok(paymentService.getPaymentByReference(reference));
    }

    @GetMapping("/order/{orderNumber}")
    public ResponseEntity<PaymentResponse> getPaymentByOrderNumber(@PathVariable String orderNumber) {
        return ResponseEntity.ok(paymentService.getPaymentByOrderNumber(orderNumber));
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments(@RequestParam(required = false) String customerEmail) {
        return ResponseEntity.ok(paymentService.getAllPayments(customerEmail));
    }
}
