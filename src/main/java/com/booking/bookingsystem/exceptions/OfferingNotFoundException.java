package com.booking.bookingsystem.exceptions;

public class OfferingNotFoundException extends RuntimeException {
    public OfferingNotFoundException(String message) {
        super(message);
    }

    public OfferingNotFoundException(Long id){
        super("Offering with id " + id + " not found");
    }
}
