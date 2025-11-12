package com.batuhan.emg_service_product.dto;

import jakarta.validation.constraints.*;

public record ProductCreateRequest(
        @NotBlank(message = "The product name cannot be left blank.")
        String name,

        @Size(max = 500, message = "The description cannot exceed 500 characters.")
        String description,

        @NotNull(message = "The price cannot be empty.")
        @PositiveOrZero(message = "The price cannot be less than zero.")
        Double price,

        @NotNull(message = "The stock quantity cannot be empty.")
        @Min(value = 0, message = "The stock quantity cannot be negative.")
        Integer stock
) {}