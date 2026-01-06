package com.booking.bookingsystem.controller;

import com.booking.bookingsystem.dto.request.CreateOfferingRequest;
import com.booking.bookingsystem.dto.request.UpdateOfferingRequest;
import com.booking.bookingsystem.dto.response.OfferingResponse;
import com.booking.bookingsystem.service.OfferingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
@RequiredArgsConstructor
public class OfferingController {

    private final OfferingService offeringService;

    @GetMapping
    public ResponseEntity<List<OfferingResponse>> getAllServices(){
        List<OfferingResponse> responses = offeringService.getAllActive();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OfferingResponse> getServiceById(@PathVariable Long id){
        OfferingResponse response = offeringService.getById(id);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfferingResponse> createService(@Valid @RequestBody CreateOfferingRequest request){
        OfferingResponse response = offeringService.createService(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OfferingResponse> updateService(@PathVariable Long id, @Valid @RequestBody UpdateOfferingRequest request
    ){
        OfferingResponse response = offeringService.updateService(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteService(@PathVariable Long id){
        offeringService.deleteService(id);
        return ResponseEntity.noContent().build();
    }
}
