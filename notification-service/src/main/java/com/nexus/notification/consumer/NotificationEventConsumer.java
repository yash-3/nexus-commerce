package com.nexus.notification.consumer;

import com.nexus.notification.dispatcher.NotificationDispatcher;
import com.nexus.notification.event.OrderPlacedEvent;
import com.nexus.notification.event.PaymentCompletedEvent;
import com.nexus.notification.service.NotificationDeduplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationDeduplicationService deduplicationService;
    private final NotificationDispatcher notificationDispatcher;

    public NotificationEventConsumer(
            NotificationDeduplicationService deduplicationService,
            NotificationDispatcher notificationDispatcher) {
        this.deduplicationService = deduplicationService;
        this.notificationDispatcher = notificationDispatcher;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.order-placed:order-placed-topic}",
            groupId = "${spring.kafka.consumer.group-id:notification-service-group}"
    )
    public void consumeOrderPlacedEvent(@Payload OrderPlacedEvent event) {
        log.info("Notification Service received OrderPlacedEvent for order: {}", event.orderNumber());

        String deduplicationKey = "ORDER_PLACED:" + event.orderNumber();
        if (deduplicationService.registerEvent(deduplicationKey)) {
            notificationDispatcher.dispatchOrderConfirmation(event);
        }
    }

    @KafkaListener(
            topics = "${app.kafka.topics.payment-completed:payment-completed-topic}",
            groupId = "${spring.kafka.consumer.group-id:notification-service-group}"
    )
    public void consumePaymentCompletedEvent(@Payload PaymentCompletedEvent event) {
        log.info("Notification Service received PaymentCompletedEvent: order={}, paymentRef={}",
                event.orderNumber(), event.paymentReference());

        String deduplicationKey = "PAYMENT_COMPLETED:" + event.paymentReference();
        if (deduplicationService.registerEvent(deduplicationKey)) {
            notificationDispatcher.dispatchPaymentReceipt(event);
        }
    }
}
