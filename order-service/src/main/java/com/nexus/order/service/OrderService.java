package com.nexus.order.service;

import com.nexus.order.client.ResilientInventoryClient;
import com.nexus.order.dto.OrderCreateRequest;
import com.nexus.order.dto.OrderItemRequest;
import com.nexus.order.dto.OrderItemResponse;
import com.nexus.order.dto.OrderResponse;
import com.nexus.order.dto.client.BatchStockCheckRequest;
import com.nexus.order.dto.client.BatchStockCheckResponse;
import com.nexus.order.dto.client.ItemAvailability;
import com.nexus.order.dto.client.SkuQuantityPair;
import com.nexus.order.entity.Order;
import com.nexus.order.entity.OrderItem;
import com.nexus.order.entity.OrderStatus;
import com.nexus.order.event.OrderItemEvent;
import com.nexus.order.event.OrderPlacedEvent;
import com.nexus.order.exception.InsufficientStockException;
import com.nexus.order.exception.OrderNotFoundException;
import com.nexus.order.publisher.OrderEventPublisher;
import com.nexus.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final OrderRepository orderRepository;
    private final ResilientInventoryClient resilientInventoryClient;
    private final OrderEventPublisher orderEventPublisher;

    public OrderService(
            OrderRepository orderRepository,
            ResilientInventoryClient resilientInventoryClient,
            OrderEventPublisher orderEventPublisher) {
        this.orderRepository = orderRepository;
        this.resilientInventoryClient = resilientInventoryClient;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        log.info("Initiating checkout orchestration for customer: {}", request.customerEmail());

        // Step 1: Prepare batch inventory verification request
        List<SkuQuantityPair> stockCheckPairs = request.items().stream()
                .map(item -> new SkuQuantityPair(item.sku(), item.quantity()))
                .toList();

        // Step 2: Call Inventory Service via resilient HTTP Interface proxy
        BatchStockCheckResponse stockResponse = resilientInventoryClient.checkStockWithResilience(
                new BatchStockCheckRequest(stockCheckPairs)
        );

        // Step 3: Validate stock availability
        if (!stockResponse.allInStock()) {
            List<String> unavailable = stockResponse.items().stream()
                    .filter(item -> !item.available())
                    .map(item -> String.format("%s (requested: %d, available: %d)",
                            item.sku(), item.requestedQuantity(), item.availableQuantity()))
                    .toList();

            log.warn("Order creation aborted. Insufficient inventory for items: {}", unavailable);
            throw new InsufficientStockException(
                    "One or more items in your order exceed currently available stock",
                    unavailable
            );
        }

        // Step 4: Construct and persist Order entity in PENDING status
        String orderNumber = "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        Order order = new Order(orderNumber, request.customerEmail(), OrderStatus.PENDING);

        for (OrderItemRequest itemReq : request.items()) {
            OrderItem orderItem = new OrderItem(itemReq.sku(), itemReq.quantity(), itemReq.unitPrice());
            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order [{}] successfully persisted with total amount: {}", savedOrder.getOrderNumber(), savedOrder.getTotalAmount());

        // Step 5: Publish Kafka OrderPlacedEvent
        List<OrderItemEvent> eventItems = savedOrder.getItems().stream()
                .map(i -> new OrderItemEvent(i.getSku(), i.getQuantity(), i.getUnitPrice()))
                .toList();

        OrderPlacedEvent placedEvent = new OrderPlacedEvent(
                savedOrder.getOrderNumber(),
                savedOrder.getCustomerEmail(),
                savedOrder.getTotalAmount(),
                eventItems,
                Instant.now()
        );

        orderEventPublisher.publishOrderPlaced(placedEvent);

        // Step 6: Return standardized response
        return mapToResponse(savedOrder);
    }

    public OrderResponse getOrderById(Long id) {
        return orderRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new OrderNotFoundException("Order with ID " + id + " not found"));
    }

    public OrderResponse getOrderByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber)
                .map(this::mapToResponse)
                .orElseThrow(() -> new OrderNotFoundException("Order with order number '" + orderNumber + "' not found"));
    }

    public List<OrderResponse> getAllOrders(String customerEmail) {
        List<Order> orders = (customerEmail != null && !customerEmail.isBlank())
                ? orderRepository.findByCustomerEmailIgnoreCase(customerEmail.trim())
                : orderRepository.findAll();

        return orders.stream()
                .map(this::mapToResponse)
                .toList();
    }

    private OrderResponse mapToResponse(Order order) {
        List<OrderItemResponse> itemResponses = order.getItems().stream()
                .map(i -> new OrderItemResponse(
                        i.getId(),
                        i.getSku(),
                        i.getQuantity(),
                        i.getUnitPrice(),
                        i.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getCustomerEmail(),
                order.getStatus(),
                order.getTotalAmount(),
                itemResponses,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }
}
