package com.hotel.reservation.repository;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.hotel.reservation.enums.RoomType;
import com.hotel.reservation.model.Customer;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.Room;
import com.hotel.reservation.util.DatabaseConnection;

public class ReservationRepository {

    // Check room availability
    public boolean isRoomAvailable(int roomNumber,
                                   LocalDate checkIn,
                                   LocalDate checkOut) throws SQLException {

        String sql = """
                SELECT COUNT(*)
                FROM reservations r
                JOIN rooms rm ON r.room_id = rm.room_id
                WHERE rm.room_number = ?
                AND r.reservation_status = 'CONFIRMED'
                AND ? < r.check_out
                AND ? > r.check_in
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, roomNumber);
            statement.setDate(2, Date.valueOf(checkIn));
            statement.setDate(3, Date.valueOf(checkOut));

            try (ResultSet result = statement.executeQuery()) {
                result.next();
                return result.getInt(1) == 0;
            }
        }
    }

    // Create reservation
    public Reservation save(Customer customer, Room room,
                            LocalDate checkIn, LocalDate checkOut)
                            throws SQLException {

        String sql = """
                INSERT INTO reservations
                (customer_id, room_id, check_in, check_out,
                 total_amount, reservation_status)
                VALUES (
                    (SELECT customer_id FROM customers WHERE email = ?),
                    (SELECT room_id FROM rooms WHERE room_number = ?),
                    ?, ?, ?, 'CONFIRMED'
                )
                RETURNING reservation_id
                """;

        try (Connection connection = DatabaseConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {
                // Lock the room row to prevent concurrent bookings.
                String lockSql = """
                        SELECT room_id FROM rooms
                        WHERE room_number = ? FOR UPDATE
                        """;

                try (PreparedStatement lockStatement =
                             connection.prepareStatement(lockSql)) {

                    lockStatement.setInt(1, room.getRoomNumber());

                    try (ResultSet result = lockStatement.executeQuery()) {
                        if (!result.next()) {
                            throw new SQLException("Room not found");
                        }
                    }
                }

                // Check availability inside the same transaction.
                String availabilitySql = """
                        SELECT COUNT(*)
                        FROM reservations r
                        JOIN rooms rm ON r.room_id = rm.room_id
                        WHERE rm.room_number = ?
                        AND r.reservation_status = 'CONFIRMED'
                        AND ? < r.check_out
                        AND ? > r.check_in
                        """;

                try (PreparedStatement availabilityStatement =
                             connection.prepareStatement(availabilitySql)) {

                    availabilityStatement.setInt(1, room.getRoomNumber());
                    availabilityStatement.setDate(2, Date.valueOf(checkIn));
                    availabilityStatement.setDate(3, Date.valueOf(checkOut));

                    try (ResultSet result =
                                 availabilityStatement.executeQuery()) {

                        result.next();

                        if (result.getInt(1) > 0) {
                            throw new SQLException(
                                    "Room is unavailable for these dates");
                        }
                    }
                }

                Reservation temporaryReservation =
                        new Reservation(1, customer, room, checkIn, checkOut);

                try (PreparedStatement statement =
                             connection.prepareStatement(sql)) {

                    statement.setString(1, customer.getEmail());
                    statement.setInt(2, room.getRoomNumber());
                    statement.setDate(3, Date.valueOf(checkIn));
                    statement.setDate(4, Date.valueOf(checkOut));
                    statement.setBigDecimal(
                            5, temporaryReservation.getTotalAmount());

                    try (ResultSet result = statement.executeQuery()) {

                        if (result.next()) {
                            int generatedId =
                                    result.getInt("reservation_id");

                            connection.commit();

                            return new Reservation(
                                    generatedId, customer, room,
                                    checkIn, checkOut);
                        }
                    }
                }

                throw new SQLException("Reservation creation failed");

            } catch (SQLException | RuntimeException e) {
                connection.rollback();
                throw e;
            } finally {
                connection.setAutoCommit(true);
            }
        }
    }

    // Retrieve all reservations
    public List<Reservation> findAll() throws SQLException {

        List<Reservation> reservations = new ArrayList<>();

        String sql = """
                SELECT r.reservation_id, r.check_in, r.check_out,
                       r.reservation_status,
                       c.name, c.email, c.phone,
                       rm.room_number, rm.room_type,
                       rm.price_per_night, rm.capacity
                FROM reservations r
                JOIN customers c ON r.customer_id = c.customer_id
                JOIN rooms rm ON r.room_id = rm.room_id
                ORDER BY r.reservation_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {

                Customer customer = new Customer(
                        result.getString("name"),
                        result.getString("email"),
                        result.getString("phone"));

                Room room = new Room(
                        result.getInt("room_number"),
                        RoomType.valueOf(result.getString("room_type")),
                        result.getBigDecimal("price_per_night"),
                        result.getInt("capacity"));

                Reservation reservation = new Reservation(
                        result.getInt("reservation_id"),
                        customer,
                        room,
                        result.getDate("check_in").toLocalDate(),
                        result.getDate("check_out").toLocalDate());

                if ("CANCELLED".equals(
                        result.getString("reservation_status"))) {
                    reservation.cancel();
                }

                reservations.add(reservation);
            }
        }

        return reservations;
    }
    
    // Cancel reservation
    public void cancelReservation(int reservationId) throws SQLException {

        String sql = """
                UPDATE reservations
                SET reservation_status = 'CANCELLED'
                WHERE reservation_id = ?
                AND reservation_status = 'CONFIRMED'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, reservationId);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException(
                        "Reservation not found or already cancelled");
            }
        }
    }
}