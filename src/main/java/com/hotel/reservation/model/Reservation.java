package com.hotel.reservation.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

import com.hotel.reservation.enums.ReservationStatus;

public class Reservation {

    private final int reservationId;
    private final Customer customer;
    private final Room room;
    private final LocalDate checkIn;
    private final LocalDate checkOut;
    private final BigDecimal totalAmount;

    private ReservationStatus status;

    public Reservation(int reservationId, Customer customer, Room room,
                       LocalDate checkIn, LocalDate checkOut) {

        if (reservationId <= 0) {
            throw new IllegalArgumentException("Reservation ID must be positive");
        }

        this.reservationId = reservationId;
        this.customer = Objects.requireNonNull(customer, "Customer cannot be null");
        this.room = Objects.requireNonNull(room, "Room cannot be null");
        this.checkIn = Objects.requireNonNull(checkIn, "Check-in date cannot be null");
        this.checkOut = Objects.requireNonNull(checkOut, "Check-out date cannot be null");

        if (!checkOut.isAfter(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        this.totalAmount = room.getPricePerNight()
                .multiply(BigDecimal.valueOf(nights));

        this.status = ReservationStatus.CONFIRMED;
    }

    public int getReservationId() {
        return reservationId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Room getRoom() {
        return room;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public ReservationStatus getStatus() {
        return status;
    }

    public void cancel() {
        if (status == ReservationStatus.CANCELLED) {
            throw new IllegalStateException("Reservation is already cancelled");
        }

        status = ReservationStatus.CANCELLED;
    }

    public void displayReservationDetails() {
        System.out.println("Reservation ID: " + reservationId);
        System.out.println("Customer: " + customer.getName());
        System.out.println("Room Number: " + room.getRoomNumber());
        System.out.println("Check-in: " + checkIn);
        System.out.println("Check-out: " + checkOut);
        System.out.println("Total Amount: " + totalAmount);
        System.out.println("Status: " + status);
    }
}