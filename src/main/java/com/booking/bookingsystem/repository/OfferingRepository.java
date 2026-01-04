package com.booking.bookingsystem.repository;

import com.booking.bookingsystem.model.Offering;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OfferingRepository extends JpaRepository<Offering, Long> {

    List<Offering> findAllByActiveTrue();
}
