package com.nexus.order.publisher;

import com.nexus.order.event.OrderPlacedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class OrderEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(OrderEventPublisher.class);

    private final KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate;
    private final String orderPlacedTopic;

    public OrderEventPublisher(
            KafkaTemplate<String, OrderPlacedEvent> kafkaTemplate,
            @Value("${app.kafka.topics.order-placed:order-placed-topic}") String orderPlacedTopic) {
        this.kafkaTemplate = kafkaTemplate;
        this.orderPlacedTopic = orderPlacedTopic;
    }

    public void publishOrderPlaced(OrderPlacedEvent event) {
        log.info("Publishing OrderPlacedEvent for orderNumber: {} to topic: {}", event.orderNumber(), orderPlacedTopic);

        CompletableFuture<SendResult<String, OrderPlacedEvent>> future =
                kafkaTemplate.send(orderPlacedTopic, event.orderNumber(), event);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("Successfully published OrderPlacedEvent [orderNumber: {}] to partition: {} with offset: {}",
                        event.orderNumber(),
                        result.getRecordMetadata().partition(),
                        result.getRecordMetadata().offset());
            } else {
                log.error("Failed to publish OrderPlacedEvent for orderNumber: {}", event.orderNumber(), ex);
            }
        });
    }
}
