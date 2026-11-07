package com.hotel.reservation.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.hotel.reservation.enums.PaymentStatus;
import com.hotel.reservation.model.Payment;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.util.DatabaseConnection;

public class PaymentRepository {

    // Save a new payment
    public Payment save(Reservation reservation) throws SQLException {

        Payment temporaryPayment = new Payment(1, reservation);

        String sql = """
                INSERT INTO payments
                (reservation_id, amount, payment_status)
                VALUES (?, ?, 'PENDING')
                RETURNING payment_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, reservation.getReservationId());
            statement.setBigDecimal(2, temporaryPayment.getAmount());

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return new Payment(
                            result.getInt("payment_id"), reservation);
                }
            }
        }

        throw new SQLException("Payment creation failed");
    }

    // Update payment status
    public void updateStatus(int paymentId, PaymentStatus status)
            throws SQLException {

        String sql = """
                UPDATE payments
                SET payment_status = ?
                WHERE payment_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, status.name());
            statement.setInt(2, paymentId);

            int rowsUpdated = statement.executeUpdate();

            if (rowsUpdated == 0) {
                throw new SQLException("Payment not found");
            }
        }
    }

    // Find payment by ID
    public PaymentStatus findStatusById(int paymentId)
            throws SQLException {

        String sql = """
                SELECT payment_status
                FROM payments
                WHERE payment_id = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, paymentId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return PaymentStatus.valueOf(
                            result.getString("payment_status"));
                }
            }
        }

        return null;
    }
    
    public void refundCompletedPaymentsForReservation(int reservationId)
            throws SQLException {

        String sql = """
                UPDATE payments
                SET payment_status = 'REFUNDED'
                WHERE reservation_id = ?
                AND payment_status = 'COMPLETED'
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, reservationId);
            statement.executeUpdate();
        }
    }
}