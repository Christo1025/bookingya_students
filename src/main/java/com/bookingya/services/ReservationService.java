package com.bookingya.services;

import com.bookingya.dtos.ReservationDto;

import java.util.UUID;

public interface ReservationService {

    Object create(ReservationDto dto);

    Object getById(UUID id);

    Object update(UUID id, ReservationDto dto);

    void delete(UUID id);
}