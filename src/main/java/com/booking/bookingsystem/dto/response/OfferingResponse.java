package com.booking.bookingsystem.dto.response;

import com.booking.bookingsystem.model.Offering;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OfferingResponse(
        Long id,
        String name,
        String description,
        Integer durationMinutes,
        BigDecimal price,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static OfferingResponse fromEntity(Offering offering){
        return new OfferingResponse(
                offering.getId(),
                offering.getName(),
                offering.getDescription(),
                offering.getDurationMinutes(),
                offering.getPrice(),
                offering.isActive(),
                offering.getCreatedAt(),
                offering.getUpdatedAt()
        );
    }
}
