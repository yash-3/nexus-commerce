package com.nexus.payment.repository;

import com.nexus.payment.entity.PaymentStatus;
import com.nexus.payment.entity.PaymentTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<PaymentTransaction, Long> {

    Optional<PaymentTransaction> findByPaymentReference(String paymentReference);

    Optional<PaymentTransaction> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumberAndStatus(String orderNumber, PaymentStatus status);

    List<PaymentTransaction> findByCustomerEmailIgnoreCase(String customerEmail);
}
