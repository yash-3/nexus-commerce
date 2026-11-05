package com.nexus.order.exception;

import java.util.List;

public class InsufficientStockException extends RuntimeException {

    private final List<String> unavailableSkus;

    public InsufficientStockException(String message, List<String> unavailableSkus) {
        super(message);
        this.unavailableSkus = unavailableSkus;
    }

    public List<String> getUnavailableSkus() {
        return unavailableSkus;
    }
}
