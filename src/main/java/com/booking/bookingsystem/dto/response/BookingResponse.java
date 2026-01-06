package com.booking.bookingsystem.dto.response;

import com.booking.bookingsystem.model.Booking;
import com.booking.bookingsystem.model.BookingStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record BookingResponse(
        Long id,
        Long userId,
        String userName,
        Long offeringId,
        String offeringName,
        LocalDate bookingDate,
        LocalTime startTime,
        BookingStatus status,
        LocalDateTime createdAt
) {
    public static BookingResponse fromBooking(Booking booking){
        return new BookingResponse(
                booking.getId(),
                booking.getUser().getId(),
                booking.getUser().getName(),
                booking.getOffering().getId(),
                booking.getOffering().getName(),
                booking.getBookingDate(),
                booking.getStartTime(),
                booking.getStatus(),
                booking.getCreatedAt()
        );
    }
}
