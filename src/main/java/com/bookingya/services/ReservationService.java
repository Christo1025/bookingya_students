package com.bookingya.services;

import com.bookingya.dtos.ReservationDto;
import com.bookingya.models.Reservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ReservationService {

    Reservation create(ReservationDto dto);

    List<Reservation> getAll();

    Reservation getById(UUID id);

    List<Reservation> getByRoomId(UUID roomId);

    List<Reservation> getByGuestId(UUID guestId);

    boolean isRoomAvailable(UUID roomId, LocalDateTime checkIn, LocalDateTime checkOut);

    Reservation update(UUID id, ReservationDto dto);

    void delete(UUID id);
}
