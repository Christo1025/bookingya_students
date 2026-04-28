package com.bookingya.dtos;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReservationDto {

    @NotNull
    private UUID roomId;

    @NotNull
    private UUID guestId;

    @NotNull
    private LocalDateTime checkIn;

    @NotNull
    private LocalDateTime checkOut;

    @NotNull
    @Min(1)
    private Integer guestsCount;

    private String notes;
}