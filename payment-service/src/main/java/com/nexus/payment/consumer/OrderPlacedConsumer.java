package com.nexus.payment.consumer;

import com.nexus.payment.event.OrderPlacedEvent;
import com.nexus.payment.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class OrderPlacedConsumer {

    private static final Logger log = LoggerFactory.getLogger(OrderPlacedConsumer.class);

    private final PaymentService paymentService;

    public OrderPlacedConsumer(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @KafkaListener(
            topics = "${app.kafka.topics.order-placed:order-placed-topic}",
            groupId = "${spring.kafka.consumer.group-id:payment-service-group}"
    )
    public void consumeOrderPlacedEvent(@Payload OrderPlacedEvent event) {
        log.info("Received OrderPlacedEvent from Kafka: orderNumber={}, customerEmail={}, amount={}",
                event.orderNumber(), event.customerEmail(), event.totalAmount());
        try {
            paymentService.processOrderPayment(event);
        } catch (Exception e) {
            log.error("Error processing payment for order: {}", event.orderNumber(), e);
            throw e;
        }
    }
}
