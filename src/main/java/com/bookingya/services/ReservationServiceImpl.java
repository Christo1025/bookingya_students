package com.bookingya.services;

import java.time.LocalDateTime;
import java.lang.reflect.Type;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;

import com.bookingya.dtos.ReservationDto;
import com.bookingya.entities.GuestEntity;
import com.bookingya.entities.ReservationEntity;
import com.bookingya.entities.RoomEntity;
import com.bookingya.exceptions.BusinessRuleException;
import com.bookingya.exceptions.EntityNotExistsException;
import com.bookingya.models.Reservation;
import com.bookingya.repositories.IGuestRepository;
import com.bookingya.repositories.IReservationRepository;
import com.bookingya.repositories.IRoomRepository;
import com.bookingya.shared.Constants;

@Service
public class ReservationServiceImpl implements ReservationService {

    private final IReservationRepository reservationRepository;
    private final IRoomRepository roomRepository;
    private final IGuestRepository guestRepository;
    private final ModelMapper mapper;

    public ReservationServiceImpl(
        IReservationRepository reservationRepository,
        IRoomRepository roomRepository,
        IGuestRepository guestRepository,
        ModelMapper mapper
    ) {
        this.reservationRepository = reservationRepository;
        this.roomRepository = roomRepository;
        this.guestRepository = guestRepository;
        this.mapper = mapper;
    }

    @Override
    public Reservation create(ReservationDto dto) {
        validateReservation(dto, null);

        ReservationEntity entity = mapper.map(dto, ReservationEntity.class);
        ReservationEntity saved = reservationRepository.save(entity);
        return mapper.map(saved, Reservation.class);
    }

    @Override
    public List<Reservation> getAll() {
        List<ReservationEntity> reservations = reservationRepository.findAll();
        Type listType = new TypeToken<List<Reservation>>() {}.getType();
        return mapper.map(reservations, listType);
    }

    @Override
    public Reservation getById(UUID id) {
        ReservationEntity reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotExistsException(Constants.RESERVATION_NOT_FOUND));

        return mapper.map(reservation, Reservation.class);
    }

    @Override
    public List<Reservation> getByRoomId(UUID roomId) {
        if (!roomRepository.existsById(roomId)) {
            throw new EntityNotExistsException(Constants.ROOM_NOT_FOUND);
        }

        List<ReservationEntity> reservations = reservationRepository.findByRoomId(roomId);
        Type listType = new TypeToken<List<Reservation>>() {}.getType();
        return mapper.map(reservations, listType);
    }

    @Override
    public List<Reservation> getByGuestId(UUID guestId) {
        if (!guestRepository.existsById(guestId)) {
            throw new EntityNotExistsException(Constants.GUEST_NOT_FOUND);
        }

        List<ReservationEntity> reservations = reservationRepository.findByGuestId(guestId);
        Type listType = new TypeToken<List<Reservation>>() {}.getType();
        return mapper.map(reservations, listType);
    }

    @Override
    public boolean isRoomAvailable(UUID roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        if (!checkIn.isBefore(checkOut)) {
            throw new BusinessRuleException(Constants.INVALID_RESERVATION_RANGE);
        }

        RoomEntity room = roomRepository.findById(roomId)
            .orElseThrow(() -> new EntityNotExistsException(Constants.ROOM_NOT_FOUND));

        return Boolean.TRUE.equals(room.getAvailable())
            && !reservationRepository.existsOverlappingReservationForRoom(roomId, checkIn, checkOut, null);
    }

    @Override
    public Reservation update(UUID id, ReservationDto dto) {
        ReservationEntity existing = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotExistsException(Constants.RESERVATION_NOT_FOUND));

        validateReservation(dto, id);
        mapper.map(dto, existing);

        ReservationEntity updated = reservationRepository.save(existing);
        return mapper.map(updated, Reservation.class);
    }

    @Override
    public void delete(UUID id) {
        ReservationEntity reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new EntityNotExistsException(Constants.RESERVATION_NOT_FOUND));

        reservationRepository.delete(reservation);
    }

    private void validateReservation(ReservationDto dto, UUID excludeId) {
        if (!dto.getCheckIn().isBefore(dto.getCheckOut())) {
            throw new BusinessRuleException(Constants.INVALID_RESERVATION_RANGE);
        }

        if (dto.getGuestsCount() == null || dto.getGuestsCount() <= 0) {
            throw new BusinessRuleException(Constants.INVALID_GUESTS_COUNT);
        }

        RoomEntity room = roomRepository.findById(dto.getRoomId())
            .orElseThrow(() -> new EntityNotExistsException(Constants.ROOM_NOT_FOUND));

        GuestEntity guest = guestRepository.findById(dto.getGuestId())
            .orElseThrow(() -> new EntityNotExistsException(Constants.GUEST_NOT_FOUND));

        if (!Boolean.TRUE.equals(room.getAvailable())) {
            throw new BusinessRuleException(Constants.ROOM_NOT_AVAILABLE);
        }

        if (dto.getGuestsCount() > room.getMaxGuests()) {
            throw new BusinessRuleException(Constants.ROOM_CAPACITY_EXCEEDED);
        }

        boolean roomOverlaps = reservationRepository.existsOverlappingReservationForRoom(
            room.getId(),
            dto.getCheckIn(),
            dto.getCheckOut(),
            excludeId
        );

        if (roomOverlaps) {
            throw new BusinessRuleException(Constants.RESERVATION_OVERLAP_ROOM);
        }

        boolean guestOverlaps = reservationRepository.existsOverlappingReservationForGuest(
            guest.getId(),
            dto.getCheckIn(),
            dto.getCheckOut(),
            excludeId
        );

        if (guestOverlaps) {
            throw new BusinessRuleException(Constants.RESERVATION_OVERLAP_GUEST);
        }
    }
}
