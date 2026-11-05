package com.nexus.inventory.controller;

import com.nexus.inventory.dto.*;
import com.nexus.inventory.service.InventoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/inventory")
public class InventoryController {

    private final InventoryService inventoryService;

    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping
    public ResponseEntity<InventoryResponse> createItem(@Valid @RequestBody StockCreateRequest request) {
        InventoryResponse created = inventoryService.createItem(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{sku}")
    public ResponseEntity<InventoryResponse> getItemBySku(@PathVariable String sku) {
        return ResponseEntity.ok(inventoryService.getItemBySku(sku));
    }

    @GetMapping
    public ResponseEntity<List<InventoryResponse>> getAllItems() {
        return ResponseEntity.ok(inventoryService.getAllItems());
    }

    @PutMapping("/{sku}")
    public ResponseEntity<InventoryResponse> updateStock(
            @PathVariable String sku,
            @Valid @RequestBody StockUpdateRequest request) {
        return ResponseEntity.ok(inventoryService.updateStock(sku, request));
    }

    @PostMapping("/reserve")
    public ResponseEntity<InventoryResponse> reserveStock(@Valid @RequestBody StockOperationRequest request) {
        return ResponseEntity.ok(inventoryService.reserveStock(request));
    }

    @PostMapping("/release")
    public ResponseEntity<InventoryResponse> releaseStock(@Valid @RequestBody StockOperationRequest request) {
        return ResponseEntity.ok(inventoryService.releaseStock(request));
    }

    @PostMapping("/deduct")
    public ResponseEntity<InventoryResponse> deductStock(@Valid @RequestBody StockOperationRequest request) {
        return ResponseEntity.ok(inventoryService.deductStock(request));
    }

    @PostMapping("/check")
    public ResponseEntity<BatchStockCheckResponse> checkBatchAvailability(@Valid @RequestBody BatchStockCheckRequest request) {
        return ResponseEntity.ok(inventoryService.checkBatchAvailability(request));
    }

    @DeleteMapping("/{sku}")
    public ResponseEntity<Void> deleteItem(@PathVariable String sku) {
        inventoryService.deleteItem(sku);
        return ResponseEntity.noContent().build();
    }
}
