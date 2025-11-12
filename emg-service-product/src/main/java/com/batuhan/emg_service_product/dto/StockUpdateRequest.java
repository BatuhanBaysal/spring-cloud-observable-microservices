package com.batuhan.emg_service_product.dto;

import jakarta.validation.constraints.NotNull;

public record StockUpdateRequest(
        @NotNull(message = "The quantity change cannot be empty.")
        Integer quantityChange
) {}