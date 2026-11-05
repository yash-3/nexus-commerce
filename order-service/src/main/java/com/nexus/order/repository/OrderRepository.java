package com.nexus.order.repository;

import com.nexus.order.entity.Order;
import com.nexus.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    boolean existsByOrderNumber(String orderNumber);

    List<Order> findByCustomerEmailIgnoreCase(String customerEmail);

    List<Order> findByStatus(OrderStatus status);
}
