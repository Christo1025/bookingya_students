package com.bookingya.tdd;

import com.bookingya.controllers.ReservationController;
import com.bookingya.dtos.ReservationDto;
import com.bookingya.models.Reservation;
import com.bookingya.services.ReservationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ReservationController.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ReservationService reservationService;

    UUID roomId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    UUID guestId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
    UUID reservationId = UUID.fromString("550e8400-e29b-41d4-a716-446655440010");

    private ReservationDto buildDto() {
        ReservationDto dto = new ReservationDto();
        dto.setRoomId(roomId);
        dto.setGuestId(guestId);
        dto.setCheckIn(LocalDateTime.parse("2026-04-20T10:00:00"));
        dto.setCheckOut(LocalDateTime.parse("2026-04-25T10:00:00"));
        dto.setGuestsCount(2);
        dto.setNotes("Ocean view requested");
        return dto;
    }

    private Reservation buildReservation() {
        Reservation reservation = new Reservation();
        reservation.setId(reservationId);
        reservation.setRoomId(roomId);
        reservation.setGuestId(guestId);
        reservation.setCheckIn(LocalDateTime.parse("2026-04-20T10:00:00"));
        reservation.setCheckOut(LocalDateTime.parse("2026-04-25T10:00:00"));
        reservation.setGuestsCount(2);
        reservation.setNotes("Ocean view requested");
        return reservation;
    }

    // 1. CREACIÓN DE RESERVA
    @Test
    void shouldCreateReservation() throws Exception {
        when(reservationService.create(any())).thenReturn(buildReservation());

        mockMvc.perform(post("/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "roomId":"%s",
                          "guestId":"%s",
                          "checkIn":"2026-04-20T10:00:00",
                          "checkOut":"2026-04-25T10:00:00",
                          "guestsCount":2,
                          "notes":"Ocean view requested"
                        }
                        """.formatted(roomId, guestId)))
                .andExpect(status().isOk());
    }

    // 2. CONSULTA DE RESERVA (GET BY ID)
    @Test
    void shouldGetReservationById() throws Exception {
        when(reservationService.getById(reservationId)).thenReturn(buildReservation());

        mockMvc.perform(get("/reservation/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId.toString()))
                .andExpect(jsonPath("$.guestId").value(guestId.toString()));
    }

    // 3. ACTUALIZACIÓN DE RESERVA
    @Test
    void shouldUpdateReservation() throws Exception {
        when(reservationService.update(eq(reservationId), any())).thenReturn(buildReservation());

        mockMvc.perform(put("/reservation/{id}", reservationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                        {
                          "roomId":"%s",
                          "guestId":"%s",
                          "checkIn":"2026-04-20T10:00:00",
                          "checkOut":"2026-04-28T10:00:00",
                          "guestsCount":2,
                          "notes":"Updated reservation"
                        }
                        """.formatted(roomId, guestId)))
                .andExpect(status().isOk());
    }

    // 4. ELIMINACIÓN DE RESERVA
    @Test
    void shouldDeleteReservation() throws Exception {
        mockMvc.perform(delete("/reservation/{id}", reservationId))
                .andExpect(status().isOk());
    }

    // 5. OBTENCIÓN DE RESERVA POR ID
    @Test
    void shouldValidateReservationById() throws Exception {
        when(reservationService.getById(reservationId)).thenReturn(buildReservation());

        mockMvc.perform(get("/reservation/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.roomId").value(roomId.toString()))
                .andExpect(jsonPath("$.guestId").value(guestId.toString()))
                .andExpect(jsonPath("$.checkIn").value("2026-04-20T10:00:00"))
                .andExpect(jsonPath("$.checkOut").value("2026-04-25T10:00:00"))
                .andExpect(jsonPath("$.guestsCount").value(2));
    }
}
