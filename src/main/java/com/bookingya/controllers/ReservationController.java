package com.bookingya.controllers;

import com.bookingya.dtos.ReservationDto;
import com.bookingya.models.Reservation;
import com.bookingya.services.ReservationService;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/reservation")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<Reservation> create(@RequestBody @Valid ReservationDto dto) {
        return ResponseEntity.ok(reservationService.create(dto));
    }

    @GetMapping
    public ResponseEntity<List<Reservation>> getAll() {
        return ResponseEntity.ok(reservationService.getAll());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Reservation> update(@PathVariable UUID id,
                                              @RequestBody @Valid ReservationDto dto) {
        return ResponseEntity.ok(reservationService.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Reservation> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(reservationService.getById(id));
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<Reservation>> getByRoomId(@PathVariable UUID roomId) {
        return ResponseEntity.ok(reservationService.getByRoomId(roomId));
    }

    @GetMapping("/guest/{guestId}")
    public ResponseEntity<List<Reservation>> getByGuestId(@PathVariable UUID guestId) {
        return ResponseEntity.ok(reservationService.getByGuestId(guestId));
    }

    @GetMapping("/availability/room/{roomId}")
    public ResponseEntity<Boolean> isRoomAvailable(@PathVariable UUID roomId,
                                                   @RequestParam LocalDateTime checkIn,
                                                   @RequestParam LocalDateTime checkOut) {
        return ResponseEntity.ok(reservationService.isRoomAvailable(roomId, checkIn, checkOut));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reservationService.delete(id);
        return ResponseEntity.ok().build();
    }
}
