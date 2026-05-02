import { expect, test } from '@playwright/test';

type Room = {
  id: string;
  code: string;
  name: string;
  city: string;
  maxGuests: number;
  nightlyPrice: number;
  available: boolean;
};

type Guest = {
  id: string;
  identification: string;
  name: string;
  email: string;
};

type Reservation = {
  id: string;
  roomId: string;
  guestId: string;
  checkIn: string;
  checkOut: string;
  guestsCount: number;
  notes: string;
};

const uniqueId = () => `${Date.now()}-${Math.random().toString(16).slice(2)}`;

test.describe('ATDD - Reservation acceptance criteria', () => {
  test('allows a user to create, consult, update and cancel a reservation', async ({ request }) => {
    const suffix = uniqueId();

    const roomResponse = await request.post('room', {
      data: {
        code: `ATDD-${suffix}`,
        name: 'ATDD acceptance room',
        city: 'Bogota',
        maxGuests: 3,
        nightlyPrice: 180000,
        available: true,
      },
    });

    expect(roomResponse.status()).toBe(200);
    const room = (await roomResponse.json()) as Room;
    expect(room.available).toBe(true);
    expect(room.maxGuests).toBeGreaterThanOrEqual(2);

    const guestResponse = await request.post('guest', {
      data: {
        identification: `ATDD-${suffix}`,
        name: 'ATDD Guest',
        email: `atdd-${suffix}@example.com`,
      },
    });

    expect(guestResponse.status()).toBe(200);
    const guest = (await guestResponse.json()) as Guest;
    expect(guest.email).toContain('@example.com');

    const createReservationResponse = await request.post('reservation', {
      data: {
        roomId: room.id,
        guestId: guest.id,
        checkIn: '2026-06-10T14:00:00',
        checkOut: '2026-06-12T11:00:00',
        guestsCount: 2,
        notes: 'Created from ATDD acceptance test',
      },
    });

    expect(createReservationResponse.status()).toBe(200);
    const createdReservation = (await createReservationResponse.json()) as Reservation;
    expect(createdReservation.id).toBeTruthy();
    expect(createdReservation.roomId).toBe(room.id);
    expect(createdReservation.guestId).toBe(guest.id);
    expect(createdReservation.guestsCount).toBe(2);

    const getReservationResponse = await request.get(`reservation/${createdReservation.id}`);

    expect(getReservationResponse.status()).toBe(200);
    const reservationById = (await getReservationResponse.json()) as Reservation;
    expect(reservationById.id).toBe(createdReservation.id);
    expect(reservationById.notes).toBe('Created from ATDD acceptance test');

    const getAllReservationsResponse = await request.get('reservation');

    expect(getAllReservationsResponse.status()).toBe(200);
    const allReservations = (await getAllReservationsResponse.json()) as Reservation[];
    expect(allReservations.some((reservation) => reservation.id === createdReservation.id)).toBe(true);

    const guestReservationsResponse = await request.get(`reservation/guest/${guest.id}`);

    expect(guestReservationsResponse.status()).toBe(200);
    const guestReservations = (await guestReservationsResponse.json()) as Reservation[];
    expect(guestReservations).toContainEqual(expect.objectContaining({
      id: createdReservation.id,
      guestId: guest.id,
    }));

    const updateReservationResponse = await request.put(`reservation/${createdReservation.id}`, {
      data: {
        roomId: room.id,
        guestId: guest.id,
        checkIn: '2026-06-10T14:00:00',
        checkOut: '2026-06-13T11:00:00',
        guestsCount: 2,
        notes: 'Updated from ATDD acceptance test',
      },
    });

    expect(updateReservationResponse.status()).toBe(200);
    const updatedReservation = (await updateReservationResponse.json()) as Reservation;
    expect(updatedReservation.checkOut).toBe('2026-06-13T11:00:00');
    expect(updatedReservation.notes).toBe('Updated from ATDD acceptance test');

    const deleteReservationResponse = await request.delete(`reservation/${createdReservation.id}`);

    expect(deleteReservationResponse.status()).toBe(200);

    const deletedReservationResponse = await request.get(`reservation/${createdReservation.id}`);

    expect(deletedReservationResponse.status()).toBe(404);
    await expect(deletedReservationResponse.json()).resolves.toEqual({
      error: 'Reservation not found',
    });
  });

  test('rejects a reservation when guest count exceeds room capacity', async ({ request }) => {
    const suffix = uniqueId();

    const roomResponse = await request.post('room', {
      data: {
        code: `ATDD-CAP-${suffix}`,
        name: 'ATDD capacity room',
        city: 'Medellin',
        maxGuests: 1,
        nightlyPrice: 150000,
        available: true,
      },
    });

    expect(roomResponse.status()).toBe(200);
    const room = (await roomResponse.json()) as Room;

    const guestResponse = await request.post('guest', {
      data: {
        identification: `ATDD-CAP-${suffix}`,
        name: 'ATDD Capacity Guest',
        email: `atdd-cap-${suffix}@example.com`,
      },
    });

    expect(guestResponse.status()).toBe(200);
    const guest = (await guestResponse.json()) as Guest;

    const invalidReservationResponse = await request.post('reservation', {
      data: {
        roomId: room.id,
        guestId: guest.id,
        checkIn: '2026-07-01T14:00:00',
        checkOut: '2026-07-03T11:00:00',
        guestsCount: 2,
        notes: 'This reservation should fail',
      },
    });

    expect(invalidReservationResponse.status()).toBe(400);
    await expect(invalidReservationResponse.json()).resolves.toEqual({
      error: 'guestsCount exceeds room capacity',
    });
  });
});
