package com.nexus.payment.publisher;

import com.nexus.payment.event.PaymentCompletedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate;
    private final String paymentCompletedTopic;

    public PaymentEventPublisher(
            KafkaTemplate<String, PaymentCompletedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.payment-completed:payment-completed-topic}") String paymentCompletedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.paymentCompletedTopic = paymentCompletedTopic;
    }

    public void publishPaymentCompleted(PaymentCompletedEvent event) {
        log.info("Publishing PaymentCompletedEvent [paymentRef: {}, orderNumber: {}] to topic: {}",
                event.paymentReference(), event.orderNumber(), paymentCompletedTopic);

        CompletableFuture<SendResult<String, PaymentCompletedEvent>> future =
                kafkaTemplate.send(paymentCompletedTopic, event.orderNumber(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published PaymentCompletedEvent to partition: {} with offset: {}",
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish PaymentCompletedEvent for order: {}", event.orderNumber(), ex);
            }
        });
    }
}
