package com.booking.bookingsystem.service;

import com.booking.bookingsystem.dto.request.CreateOfferingRequest;
import com.booking.bookingsystem.dto.request.UpdateOfferingRequest;
import com.booking.bookingsystem.dto.response.OfferingResponse;
import com.booking.bookingsystem.exceptions.OfferingNotFoundException;
import com.booking.bookingsystem.model.Offering;
import com.booking.bookingsystem.repository.OfferingRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OfferingServiceTest {

    @Mock
    private OfferingRepository offeringRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private OfferingService offeringService;

    private Offering offering;

    @BeforeEach
    void setup() {
        offering = Offering.builder()
                .id(1L)
                .name("Haircut")
                .description("Standard haircut")
                .durationMinutes(30)
                .price(BigDecimal.valueOf(50.00))
                .active(true)
                .build();
    }

    @Test
    void createOffering_ShouldReturnCreatedOffering() {
        //Given
        CreateOfferingRequest request = new CreateOfferingRequest(
                "Haircut", "Standard haircut", 30, BigDecimal.valueOf(50.00)
        );
        when(offeringRepository.save(any(Offering.class))).thenReturn(offering);

        //When
        OfferingResponse response = offeringService.createService(request);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.name()).isEqualTo(request.name());
        assertThat(response.price()).isEqualByComparingTo(BigDecimal.valueOf(50.00));
        verify(entityManager).flush();
        verify(entityManager).refresh(any());
        verify(offeringRepository, times(1)).save(any(Offering.class));
    }

    @Test
    void getAllActiveOfferings_ShouldReturnActiveOfferings() {
        //Given
        when(offeringRepository.findAllByActiveTrue()).thenReturn(List.of(offering));

        //When
        List<OfferingResponse> offerings = offeringService.getAllActive();

        //Then
        assertThat(offerings).hasSize(1);
        assertThat(offerings.getFirst().name()).isEqualTo("Haircut");
        verify(offeringRepository, times(1)).findAllByActiveTrue();
    }

    @Test
    void getAllActiveOfferings_ShouldReturnEmptyList_WhenNoActiveOfferings(){
        //Given
        when(offeringRepository.findAllByActiveTrue()).thenReturn(List.of());

        //When
        List<OfferingResponse> offerings = offeringService.getAllActive();

        //Then
        assertThat(offerings).isEmpty();
        verify(offeringRepository, times(1)).findAllByActiveTrue();
    }

    @Test
    void getOfferingById_WhenExists_ShouldReturnOffering() {
        //Given
        when(offeringRepository.findById(1L)).thenReturn(Optional.of(offering));

        //When
        OfferingResponse response = offeringService.getById(1L);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(1L);
        verify(offeringRepository, times(1)).findById(1L);
    }

    @Test
    void getOfferingById_WhenNotFound_ShouldThrowException(){
        //Given
        when(offeringRepository.findById(999L)).thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> offeringService.getById(999L))
                .isInstanceOf(OfferingNotFoundException.class)
                .hasMessageContaining("999");

        verify(offeringRepository, times(1)).findById(999L);
    }

    @Test
    void updateOffering_ShouldUpdateOffering_WhenValid() {

        //Given
        Long id = 1L;
        UpdateOfferingRequest request = new UpdateOfferingRequest(
                "Haircut Premium",
                "Premium haircut",
                45,
                BigDecimal.valueOf(75.00)
        );

        when(offeringRepository.findById(id)).thenReturn(Optional.of(offering));
        when(offeringRepository.save(any(Offering.class))).thenAnswer(i -> i.getArguments()[0]);


        //WHEN
        OfferingResponse response = offeringService.updateService(id, request);

        //Then
        assertThat(response).isNotNull();
        assertThat(response.id()).isEqualTo(id);
        assertThat(request.name()).isEqualTo(response.name());
        assertThat(request.price()).isEqualByComparingTo(response.price());
        verify(offeringRepository, times(1)).save(any(Offering.class));
    }

    @Test
    void updateOffering_ShouldThrowExceptionWhenNotFound(){


        UpdateOfferingRequest request = new UpdateOfferingRequest(
                "Haircut Premium",
                "Premium haircut",
                45,
                BigDecimal.valueOf(75.00)
        );
        when(offeringRepository.findById(999L)).thenReturn(Optional.empty());

        //When & Then
        assertThrows(OfferingNotFoundException.class, () -> offeringService.updateService(999L, request));
        verify(offeringRepository, never()).save(any());
    }

    @Test
    void deleteOffering_ShouldSoftDelete(){
        //Given
        when(offeringRepository.findById(1L)).thenReturn(Optional.of(offering));
        when(offeringRepository.save(any(Offering.class))).thenReturn(offering);

        //When
        offeringService.deleteService(1L);

        //Then
        verify(offeringRepository, times(1)).save(argThat(o -> !o.isActive()));
    }

    @Test
    void deleteOffering_ShouldThrowException_WhenNotFound(){
        //Given
        when(offeringRepository.findById(999L)).thenReturn(Optional.empty());

        //When & Then
        assertThatThrownBy(() -> offeringService.deleteService(999L))
                .isInstanceOf(OfferingNotFoundException.class);

        verify(offeringRepository, never()).save(any());
    }
}
