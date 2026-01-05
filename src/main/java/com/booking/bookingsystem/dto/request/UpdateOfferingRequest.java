package com.booking.bookingsystem.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record UpdateOfferingRequest(

        @NotBlank(message = "Name is required")
        String name,

        String description,

        @NotNull(message = "Add offering time in minutes")
        @Positive
        Integer durationMinutes,

        @NotNull
        @DecimalMin(value = "0.0", message = "Price must be greater than 0")
        BigDecimal price
) {
}
