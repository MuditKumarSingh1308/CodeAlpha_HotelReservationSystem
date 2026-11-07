package com.hotel.reservation.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.hotel.reservation.enums.RoomType;
import com.hotel.reservation.model.Room;
import com.hotel.reservation.util.DatabaseConnection;

public class RoomRepository {

    // Save a room
    public void save(Room room) throws SQLException {

        String sql = """
                INSERT INTO rooms
                (room_number, room_type, price_per_night, capacity)
                VALUES (?, ?, ?, ?)
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, room.getRoomNumber());
            statement.setString(2, room.getRoomType().name());
            statement.setBigDecimal(3, room.getPricePerNight());
            statement.setInt(4, room.getCapacity());

            statement.executeUpdate();
        }
    }

    // Find room by room number
    public Room findByRoomNumber(int roomNumber) throws SQLException {

        String sql = "SELECT * FROM rooms WHERE room_number = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, roomNumber);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapRoom(result);
                }
            }
        }

        return null;
    }

    // Retrieve all rooms
    public List<Room> findAll() throws SQLException {

        List<Room> rooms = new ArrayList<>();

        String sql = "SELECT * FROM rooms ORDER BY room_number";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                rooms.add(mapRoom(result));
            }
        }

        return rooms;
    }

    // Convert database record into Room object
    private Room mapRoom(ResultSet result) throws SQLException {

        return new Room(
                result.getInt("room_number"),
                RoomType.valueOf(result.getString("room_type")),
                result.getBigDecimal("price_per_night"),
                result.getInt("capacity")
        );
    }
    
    public List<Room> findAvailableRooms(
            LocalDate checkIn, LocalDate checkOut) throws SQLException {

        String sql = """
                SELECT rm.room_number, rm.room_type,
                       rm.price_per_night, rm.capacity
                FROM rooms rm
                WHERE NOT EXISTS (
                    SELECT 1
                    FROM reservations r
                    WHERE r.room_id = rm.room_id
                    AND r.reservation_status = 'CONFIRMED'
                    AND ? < r.check_out
                    AND ? > r.check_in
                )
                ORDER BY rm.room_number
                """;

        List<Room> rooms = new ArrayList<>();

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setDate(1, java.sql.Date.valueOf(checkIn));
            statement.setDate(2, java.sql.Date.valueOf(checkOut));

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rooms.add(mapRoom(resultSet));
                }
            }
        }

        return rooms;
    }
}