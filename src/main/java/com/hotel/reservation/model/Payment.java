package com.hotel.reservation.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

import com.hotel.reservation.enums.PaymentStatus;

public class Payment {

    private final int paymentId;
    private final Reservation reservation;
    private final BigDecimal amount;
    private final LocalDateTime paymentDate;

    private PaymentStatus status;

    public Payment(int paymentId, Reservation reservation) {

        if (paymentId <= 0) {
            throw new IllegalArgumentException("Payment ID must be positive");
        }

        this.paymentId = paymentId;
        this.reservation = Objects.requireNonNull(
                reservation, "Reservation cannot be null");

        if (reservation.getStatus().name().equals("CANCELLED")) {
            throw new IllegalArgumentException(
                    "Cannot create payment for a cancelled reservation");
        }

        this.amount = reservation.getTotalAmount();
        this.paymentDate = LocalDateTime.now();
        this.status = PaymentStatus.PENDING;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void completePayment() {

        if (status != PaymentStatus.PENDING) {
            throw new IllegalStateException(
                    "Only pending payments can be completed");
        }

        status = PaymentStatus.COMPLETED;
    }

    public void refundPayment() {

        if (status != PaymentStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Only completed payments can be refunded");
        }

        status = PaymentStatus.REFUNDED;
    }

    public void displayPaymentDetails() {
        System.out.println("Payment ID: " + paymentId);
        System.out.println("Reservation ID: " + reservation.getReservationId());
        System.out.println("Amount: " + amount);
        System.out.println("Payment Date: " + paymentDate);
        System.out.println("Payment Status: " + status);
    }
}