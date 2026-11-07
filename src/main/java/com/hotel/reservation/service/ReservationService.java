package com.hotel.reservation.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.hotel.reservation.model.Customer;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.Room;
import com.hotel.reservation.repository.ReservationRepository;

public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService() {
        this.reservationRepository = new ReservationRepository();
    }

    // Book a room
    public Reservation bookRoom(Customer customer, Room room,
                                LocalDate checkIn, LocalDate checkOut)
                                throws SQLException {

        if (customer == null || room == null) {
            throw new IllegalArgumentException(
                    "Customer and room are required");
        }

        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException(
                    "Booking dates cannot be null");
        }

        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException(
                    "Check-in date cannot be in the past");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException(
                    "Check-out must be after check-in");
        }

        boolean available = reservationRepository.isRoomAvailable(
                room.getRoomNumber(), checkIn, checkOut);

        if (!available) {
            throw new IllegalStateException(
                    "Room is unavailable for selected dates");
        }

        return reservationRepository.save(
                customer, room, checkIn, checkOut);
    }

    // Cancel booking
    public void cancelReservation(int reservationId)
            throws SQLException {

        if (reservationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid reservation ID");
        }

        reservationRepository.cancelReservation(reservationId);
    }

    // Display booking history
    public List<Reservation> getAllReservations()
            throws SQLException {

        return reservationRepository.findAll();
    }
}