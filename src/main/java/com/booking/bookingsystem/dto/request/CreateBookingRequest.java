package com.booking.bookingsystem.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateBookingRequest(
        @NotNull(message = "Service ID required")
        Long offeringId,

        @NotNull
        @FutureOrPresent
        LocalDate bookingDate,

        @NotNull
        LocalTime startTime
) {
}
