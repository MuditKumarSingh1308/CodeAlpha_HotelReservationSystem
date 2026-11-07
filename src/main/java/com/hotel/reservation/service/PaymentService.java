package com.hotel.reservation.service;

import java.sql.SQLException;

import com.hotel.reservation.enums.PaymentStatus;
import com.hotel.reservation.enums.ReservationStatus;
import com.hotel.reservation.model.Payment;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.repository.PaymentRepository;

public class PaymentService {

    private final PaymentRepository paymentRepository;

    public PaymentService() {
        this.paymentRepository = new PaymentRepository();
    }

    public Payment makePayment(Reservation reservation)
            throws SQLException {

        if (reservation == null) {
            throw new IllegalArgumentException(
                    "Reservation cannot be null");
        }

        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw new IllegalStateException(
                    "Cannot pay for a cancelled reservation");
        }

        // Create a pending payment in the database.
        Payment payment = paymentRepository.save(reservation);

        // Simulate successful payment.
        payment.completePayment();

        // Update payment status in the database.
        paymentRepository.updateStatus(
                payment.getPaymentId(),
                PaymentStatus.COMPLETED
        );

        return payment;
    }

    public void refundPayment(Payment payment)
            throws SQLException {

        if (payment == null) {
            throw new IllegalArgumentException(
                    "Payment cannot be null");
        }

        if (payment.getStatus() != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Only completed payments can be refunded");
        }

        payment.refundPayment();

        paymentRepository.updateStatus(
                payment.getPaymentId(),
                PaymentStatus.REFUNDED
        );
    }

    public void refundForReservation(int reservationId)
            throws SQLException {

        if (reservationId <= 0) {
            throw new IllegalArgumentException(
                    "Invalid reservation ID");
        }

        paymentRepository.refundCompletedPaymentsForReservation(
                reservationId);
    }
}