package com.booking.bookingsystem.repository;

import com.booking.bookingsystem.model.Booking;
import com.booking.bookingsystem.model.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findByUserIdOrderByBookingDateDescStartTimeDesc(Long userId);

    List<Booking> findByOfferingIdAndBookingDateAndStatus(
            Long offeringId,
            LocalDate bookingDate,
            BookingStatus status
    );


    @Query("SELECT b FROM Booking b WHERE b.offering.id = :offeringId " +
    "AND b.bookingDate = :date AND b.startTime = :time " +
    "AND b.status IN ('PENDING', 'CONFIRMED')")
    List<Booking> findConflictingBookings(
            @Param("offeringId") Long offeringId,
            @Param("date") LocalDate date,
            @Param("time")LocalTime time
            );

}
