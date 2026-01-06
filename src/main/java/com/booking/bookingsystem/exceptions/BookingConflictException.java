package com.booking.bookingsystem.exceptions;

import java.time.LocalDate;
import java.time.LocalTime;

public class BookingConflictException extends RuntimeException {

    public BookingConflictException(){
        super("Time slot already booked for this service");
    }

    public BookingConflictException(String message) {
        super(message);
    }
}
