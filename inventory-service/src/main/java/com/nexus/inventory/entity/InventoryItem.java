package com.nexus.inventory.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "inventory_items", indexes = {
    @Index(name = "idx_inventory_sku", columnList = "sku", unique = true)
})
@EntityListeners(AuditingEntityListener.class)
public class InventoryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 64)
    private String sku;

    @Column(nullable = false)
    private Integer availableQuantity = 0;

    @Column(nullable = false)
    private Integer reservedQuantity = 0;

    @Column(nullable = false)
    private Integer reorderThreshold = 10;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private Instant updatedAt;

    public InventoryItem() {
    }

    public InventoryItem(String sku, Integer availableQuantity, Integer reorderThreshold) {
        this.sku = sku;
        this.availableQuantity = availableQuantity != null ? availableQuantity : 0;
        this.reservedQuantity = 0;
        this.reorderThreshold = reorderThreshold != null ? reorderThreshold : 10;
    }

    public Integer getTotalQuantity() {
        return availableQuantity + reservedQuantity;
    }

    public boolean hasSufficientStock(int requestedQuantity) {
        return availableQuantity >= requestedQuantity;
    }

    public void reserve(int quantity) {
        if (!hasSufficientStock(quantity)) {
            throw new IllegalStateException("Insufficient stock to reserve " + quantity + " units for SKU: " + sku);
        }
        this.availableQuantity -= quantity;
        this.reservedQuantity += quantity;
    }

    public void releaseReservation(int quantity) {
        if (this.reservedQuantity < quantity) {
            throw new IllegalStateException("Cannot release " + quantity + " units; only " + reservedQuantity + " reserved for SKU: " + sku);
        }
        this.reservedQuantity -= quantity;
        this.availableQuantity += quantity;
    }

    public void deductReserved(int quantity) {
        if (this.reservedQuantity < quantity) {
            throw new IllegalStateException("Cannot deduct " + quantity + " units from reserve; only " + reservedQuantity + " reserved for SKU: " + sku);
        }
        this.reservedQuantity -= quantity;
    }

    public void deductDirect(int quantity) {
        if (!hasSufficientStock(quantity)) {
            throw new IllegalStateException("Insufficient stock to deduct " + quantity + " units for SKU: " + sku);
        }
        this.availableQuantity -= quantity;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public void setAvailableQuantity(Integer availableQuantity) {
        this.availableQuantity = availableQuantity;
    }

    public Integer getReservedQuantity() {
        return reservedQuantity;
    }

    public void setReservedQuantity(Integer reservedQuantity) {
        this.reservedQuantity = reservedQuantity;
    }

    public Integer getReorderThreshold() {
        return reorderThreshold;
    }

    public void setReorderThreshold(Integer reorderThreshold) {
        this.reorderThreshold = reorderThreshold;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        InventoryItem that = (InventoryItem) o;
        return Objects.equals(sku, that.sku);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku);
    }

    @Override
    public String toString() {
        return "InventoryItem{" +
                "id=" + id +
                ", sku='" + sku + '\'' +
                ", availableQuantity=" + availableQuantity +
                ", reservedQuantity=" + reservedQuantity +
                ", reorderThreshold=" + reorderThreshold +
                '}';
    }
}
