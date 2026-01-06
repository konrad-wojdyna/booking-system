package com.booking.bookingsystem.service;


import com.booking.bookingsystem.dto.request.CreateOfferingRequest;
import com.booking.bookingsystem.dto.request.UpdateOfferingRequest;
import com.booking.bookingsystem.dto.response.OfferingResponse;
import com.booking.bookingsystem.exceptions.OfferingNotFoundException;
import com.booking.bookingsystem.model.Offering;
import com.booking.bookingsystem.repository.OfferingRepository;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class OfferingService {

    private final OfferingRepository offeringRepository;
    private final EntityManager entityManager;

    @Transactional
    public OfferingResponse createService(CreateOfferingRequest request){
        log.info("Creating new service: {}", request.name());

        Offering offering = Offering.builder()
                .name(request.name())
                .description(request.description())
                .durationMinutes(request.durationMinutes())
                .price(request.price())
                .active(true)
                .build();

        Offering offeringSaved = offeringRepository.save(offering);

        entityManager.flush();
        entityManager.refresh(offeringSaved);

        log.info("Service created successfully: {} (ID: {})", offeringSaved.getName(), offeringSaved.getId());

        return OfferingResponse.fromEntity(offeringSaved);
    }

    @Transactional(readOnly = true)
    public List<OfferingResponse> getAllActive(){
        log.debug("Fetching all active services");

        List<OfferingResponse> services = offeringRepository.findAllByActiveTrue().stream()
                .map(OfferingResponse::fromEntity)
                .toList();

        log.debug("Found {} active services", services.size());

        return services;
    }

    @Transactional(readOnly = true)
    public OfferingResponse getById(Long id){
        return offeringRepository.findById(id)
                .map(OfferingResponse::fromEntity)
                .orElseThrow(() -> new OfferingNotFoundException(id));
    }

    @Transactional
    public OfferingResponse updateService(Long id, UpdateOfferingRequest request){
        log.info("Updating service with ID: {}", id);

        Offering offering = offeringRepository.findById(id).orElseThrow(() -> new OfferingNotFoundException(id));

        offering.setName(request.name());
        offering.setDescription(request.description());
        offering.setDurationMinutes(request.durationMinutes());
        offering.setPrice(request.price());

        Offering updatedOffering = offeringRepository.save(offering);

        return OfferingResponse.fromEntity(updatedOffering);
    }

    @Transactional
    public void deleteService(Long id){
        log.info("Deactivating service with ID: {}", id);

        Offering offering = offeringRepository.findById(id)
                .orElseThrow(() -> new OfferingNotFoundException(id));

        offering.setActive(false);
        offeringRepository.save(offering);

        log.info("Service deactivated successfully: {}", id);
    }
}
