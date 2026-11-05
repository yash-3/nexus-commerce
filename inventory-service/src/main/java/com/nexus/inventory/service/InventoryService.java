package com.nexus.inventory.service;

import com.nexus.inventory.dto.*;
import com.nexus.inventory.entity.InventoryItem;
import com.nexus.inventory.exception.DuplicateInventoryException;
import com.nexus.inventory.exception.InsufficientStockException;
import com.nexus.inventory.exception.InventoryNotFoundException;
import com.nexus.inventory.repository.InventoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class InventoryService {

    private final InventoryRepository inventoryRepository;

    public InventoryService(InventoryRepository inventoryRepository) {
        this.inventoryRepository = inventoryRepository;
    }

    @Transactional
    public InventoryResponse createItem(StockCreateRequest request) {
        if (inventoryRepository.existsBySku(request.sku())) {
            throw new DuplicateInventoryException("Inventory entry for SKU '" + request.sku() + "' already exists");
        }

        InventoryItem item = new InventoryItem(
                request.sku(),
                request.availableQuantity(),
                request.reorderThreshold()
        );

        InventoryItem saved = inventoryRepository.save(item);
        return mapToResponse(saved);
    }

    public InventoryResponse getItemBySku(String sku) {
        return inventoryRepository.findBySku(sku)
                .map(this::mapToResponse)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory item for SKU '" + sku + "' not found"));
    }

    public List<InventoryResponse> getAllItems() {
        return inventoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public InventoryResponse updateStock(String sku, StockUpdateRequest request) {
        InventoryItem item = inventoryRepository.findBySkuWithLock(sku)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory item for SKU '" + sku + "' not found"));

        item.setAvailableQuantity(request.availableQuantity());
        item.setReorderThreshold(request.reorderThreshold());

        InventoryItem updated = inventoryRepository.save(item);
        return mapToResponse(updated);
    }

    @Transactional
    public InventoryResponse reserveStock(StockOperationRequest request) {
        InventoryItem item = inventoryRepository.findBySkuWithLock(request.sku())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory item for SKU '" + request.sku() + "' not found"));

        if (!item.hasSufficientStock(request.quantity())) {
            throw new InsufficientStockException(String.format(
                    "Insufficient stock for SKU '%s': requested %d, available %d",
                    request.sku(), request.quantity(), item.getAvailableQuantity()
            ));
        }

        item.reserve(request.quantity());
        InventoryItem updated = inventoryRepository.save(item);
        return mapToResponse(updated);
    }

    @Transactional
    public InventoryResponse releaseStock(StockOperationRequest request) {
        InventoryItem item = inventoryRepository.findBySkuWithLock(request.sku())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory item for SKU '" + request.sku() + "' not found"));

        if (item.getReservedQuantity() < request.quantity()) {
            throw new InsufficientStockException(String.format(
                    "Cannot release %d units for SKU '%s'; only %d units currently reserved",
                    request.quantity(), request.sku(), item.getReservedQuantity()
            ));
        }

        item.releaseReservation(request.quantity());
        InventoryItem updated = inventoryRepository.save(item);
        return mapToResponse(updated);
    }

    @Transactional
    public InventoryResponse deductStock(StockOperationRequest request) {
        InventoryItem item = inventoryRepository.findBySkuWithLock(request.sku())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory item for SKU '" + request.sku() + "' not found"));

        if (item.getReservedQuantity() >= request.quantity()) {
            item.deductReserved(request.quantity());
        } else if (item.hasSufficientStock(request.quantity())) {
            item.deductDirect(request.quantity());
        } else {
            throw new InsufficientStockException(String.format(
                    "Cannot deduct %d units for SKU '%s': available %d, reserved %d",
                    request.quantity(), request.sku(), item.getAvailableQuantity(), item.getReservedQuantity()
            ));
        }

        InventoryItem updated = inventoryRepository.save(item);
        return mapToResponse(updated);
    }

    public BatchStockCheckResponse checkBatchAvailability(BatchStockCheckRequest request) {
        Set<String> skus = request.items().stream()
                .map(SkuQuantityPair::sku)
                .collect(Collectors.toSet());

        List<InventoryItem> items = inventoryRepository.findBySkuIn(skus);
        Map<String, InventoryItem> itemMap = items.stream()
                .collect(Collectors.toMap(InventoryItem::getSku, i -> i));

        boolean allAvailable = true;
        List<ItemAvailability> availabilityList = new ArrayList<>();

        for (SkuQuantityPair pair : request.items()) {
            InventoryItem item = itemMap.get(pair.sku());
            int available = (item != null) ? item.getAvailableQuantity() : 0;
            boolean hasStock = (item != null) && item.hasSufficientStock(pair.quantity());

            if (!hasStock) {
                allAvailable = false;
            }

            availabilityList.add(new ItemAvailability(
                    pair.sku(),
                    pair.quantity(),
                    available,
                    hasStock
            ));
        }

        return new BatchStockCheckResponse(allAvailable, availabilityList);
    }

    @Transactional
    public void deleteItem(String sku) {
        InventoryItem item = inventoryRepository.findBySku(sku)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory item for SKU '" + sku + "' not found"));
        inventoryRepository.delete(item);
    }

    private InventoryResponse mapToResponse(InventoryItem item) {
        return new InventoryResponse(
                item.getId(),
                item.getSku(),
                item.getAvailableQuantity(),
                item.getReservedQuantity(),
                item.getTotalQuantity(),
                item.getReorderThreshold(),
                item.getAvailableQuantity() > 0,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }
}
