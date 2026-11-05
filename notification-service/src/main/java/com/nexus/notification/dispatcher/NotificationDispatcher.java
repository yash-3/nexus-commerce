package com.nexus.notification.dispatcher;

import com.nexus.notification.event.OrderItemEvent;
import com.nexus.notification.event.OrderPlacedEvent;
import com.nexus.notification.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class NotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(NotificationDispatcher.class);

    public void dispatchOrderConfirmation(OrderPlacedEvent event) {
        String itemsSummary = (event.items() != null)
                ? event.items().stream()
                    .map(i -> String.format("- %s (Qty: %d @ $%s)", i.sku(), i.quantity(), i.unitPrice()))
                    .collect(Collectors.joining("\n"))
                : "No items listed";

        String emailBody = String.format("""
                
                ================================================================================
                [EMAIL DISPATCH] ORDER CONFIRMATION
                To: %s
                Subject: Nexus Commerce - Order Placed Successfully (%s)
                --------------------------------------------------------------------------------
                Dear Customer,
                
                Thank you for your order! We have received your order and it is being processed.
                
                Order Number: %s
                Total Amount: $%s
                Items Ordered:
                %s
                
                We will notify you once payment is confirmed and items are dispatched.
                ================================================================================
                """,
                event.customerEmail(),
                event.orderNumber(),
                event.orderNumber(),
                event.totalAmount(),
                itemsSummary
        );

        log.info(emailBody);
    }

    public void dispatchPaymentReceipt(PaymentCompletedEvent event) {
        String emailBody = String.format("""
                
                ================================================================================
                [EMAIL DISPATCH] PAYMENT RECEIPT
                To: %s
                Subject: Nexus Commerce - Payment %s for Order %s
                --------------------------------------------------------------------------------
                Dear Customer,
                
                Your payment has been processed.
                
                Payment Reference: %s
                Order Number:      %s
                Amount Charged:    $%s
                Status:            %s
                Date:              %s
                
                Thank you for shopping with Nexus Commerce!
                ================================================================================
                """,
                event.customerEmail(),
                event.status(),
                event.orderNumber(),
                event.paymentReference(),
                event.orderNumber(),
                event.amount(),
                event.status(),
                event.timestamp()
        );

        log.info(emailBody);

        // Also simulate an SMS alert
        dispatchSmsAlert(event.customerEmail(), String.format(
                "[NEXUS] Payment of $%s for Order %s was %s. Ref: %s",
                event.amount(), event.orderNumber(), event.status(), event.paymentReference()
        ));
    }

    public void dispatchSmsAlert(String recipient, String message) {
        log.info("[SMS GATEWAY] Dispatching SMS to {}: {}", recipient, message);
    }
}
