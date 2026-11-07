package com.hotel.reservation.service;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import com.hotel.reservation.model.Room;
import com.hotel.reservation.repository.RoomRepository;

public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService() {
        this.roomRepository = new RoomRepository();
    }

    public void addRoom(Room room) throws SQLException {
        if (room == null) {
            throw new IllegalArgumentException("Room cannot be null");
        }

        if (roomRepository.findByRoomNumber(room.getRoomNumber()) != null) {
            throw new IllegalArgumentException("Room already exists");
        }

        roomRepository.save(room);
    }

    public Room findRoom(int roomNumber) throws SQLException {
        if (roomNumber <= 0) {
            throw new IllegalArgumentException("Invalid room number");
        }

        return roomRepository.findByRoomNumber(roomNumber);
    }

    public List<Room> getAllRooms() throws SQLException {
        return roomRepository.findAll();
    }
    
    public List<Room> findAvailableRooms(
            LocalDate checkIn, LocalDate checkOut) throws SQLException {

        if (checkIn == null || checkOut == null) {
            throw new IllegalArgumentException("Booking dates are required");
        }

        if (checkIn.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Check-in cannot be in the past");
        }

        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException(
                    "Check-out must be after check-in");
        }

        return roomRepository.findAvailableRooms(checkIn, checkOut);
    }
}