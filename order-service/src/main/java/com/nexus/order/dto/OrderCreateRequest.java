package com.nexus.order.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

public record OrderCreateRequest(
    @NotBlank(message = "Customer email is mandatory")
    @Email(message = "Customer email must be a valid email address")
    String customerEmail,

    @NotEmpty(message = "Order must contain at least one item")
    List<@Valid OrderItemRequest> items
) {}
