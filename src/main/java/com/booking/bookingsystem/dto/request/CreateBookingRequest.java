package com.booking.bookingsystem.dto.request;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalTime;

public record CreateBookingRequest(
        @NotNull(message = "Service ID required")
        Long offeringId,

        @NotNull(message = "Booking date is required")
        @Future(message = "Booking date must be in the future")
        LocalDate bookingDate,

        @NotNull(message = "Start time is required")
        LocalTime startTime
) {
}
