package com.booking.bookingsystem.service;

import com.booking.bookingsystem.dto.request.CreateBookingRequest;
import com.booking.bookingsystem.dto.response.BookingResponse;
import com.booking.bookingsystem.exceptions.BookingConflictException;
import com.booking.bookingsystem.exceptions.BookingNotFoundException;
import com.booking.bookingsystem.exceptions.OfferingNotFoundException;
import com.booking.bookingsystem.exceptions.UserNotFoundException;
import com.booking.bookingsystem.model.*;
import com.booking.bookingsystem.repository.BookingRepository;
import com.booking.bookingsystem.repository.OfferingRepository;
import com.booking.bookingsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class BookingService {

    private final BookingRepository bookingRepository;
    private final UserRepository userRepository;
    private final OfferingRepository offeringRepository;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request, String userEmail){

        log.info("Creating booking for user: {}", userEmail);

        User user = getUserByEmail(userEmail);

        Offering offering = offeringRepository.findById(request.offeringId()).orElseThrow(() -> new
                OfferingNotFoundException(request.offeringId()));

        List<Booking> conflictBookings = bookingRepository.findConflictingBookings(
                request.offeringId(),
                request.bookingDate(),
                request.startTime());

        if(!conflictBookings.isEmpty()){
            throw new BookingConflictException();
        }

        Booking booking = Booking.builder()
                .user(user)
                .offering(offering)
                .bookingDate(request.bookingDate())
                .startTime(request.startTime())
                .status(BookingStatus.PENDING)
                .build();

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking created with id: {}", savedBooking.getId());

        return BookingResponse.fromBooking(savedBooking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(String userEmail){
        log.info("Fetching bookings for user: {}", userEmail);

        User user = getUserByEmail(userEmail);

        return bookingRepository.findByUserIdOrderByBookingDateDescStartTimeDesc(user.getId())
                .stream()
                .map(BookingResponse::fromBooking)
                .toList();
    }

    @Transactional(readOnly = true)
    public BookingResponse getById(Long bookingId, String userEmail){
        log.info("Fetching booking by id: {}", bookingId);

        Booking booking = getBookingById(bookingId);

        //Verify user owns this booking (or is admin)
        User user = userRepository.findByEmail(userEmail).orElseThrow(() ->
                new UserNotFoundException("User not found"));

        checkIsUserResourceOrIsAdmin(booking, user);

        return BookingResponse.fromBooking(booking);
    }

    //cancel
    @Transactional
    public void cancelBooking(Long bookingId, String userEmail){
        log.info("Cancelling booking with id: {}", bookingId);

        Booking booking = getBookingById(bookingId);

        User user = getUserByEmail(userEmail);

        checkIsUserResourceOrIsAdmin(booking, user);

        if(booking.getStatus() == BookingStatus.CANCELLED){
            throw new IllegalStateException("Booking is already cancelled");
        }

        if(booking.getBookingDate().isBefore(LocalDate.now())){
            throw new IllegalStateException("Cannot cancel past bookings");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        bookingRepository.save(booking);
        log.info("Booking cancelled: {}", bookingId);
    }

    private User getUserByEmail(String userEmail){
        return userRepository.findByEmail(userEmail).orElseThrow(() -> new UserNotFoundException("User not found with email: " + userEmail));
    }

    private Booking getBookingById(Long bookingId){
        return bookingRepository.findById(bookingId).orElseThrow(() -> new BookingNotFoundException(bookingId));
    }

    private void checkIsUserResourceOrIsAdmin(Booking booking, User user){
        if(!booking.getUser().getId().equals(user.getId()) && !user.getRole().equals(Role.ADMIN)){
            throw new BookingNotFoundException(booking.getId());
        }
    }

}
