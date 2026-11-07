package com.hotel.reservation.ui;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Scanner;

import com.hotel.reservation.enums.RoomType;
import com.hotel.reservation.model.Customer;
import com.hotel.reservation.model.Payment;
import com.hotel.reservation.model.Reservation;
import com.hotel.reservation.model.Room;
import com.hotel.reservation.service.CustomerService;
import com.hotel.reservation.service.PaymentService;
import com.hotel.reservation.service.ReservationService;
import com.hotel.reservation.service.RoomService;

public class HotelReservationApp {

    private static final Scanner scanner = new Scanner(System.in);

    private static final RoomService roomService = new RoomService();
    private static final CustomerService customerService = new CustomerService();
    private static final ReservationService reservationService =
            new ReservationService();
    private static final PaymentService paymentService =
            new PaymentService();

    public static void main(String[] args) {

        System.out.println("======================================");
        System.out.println("       HOTEL RESERVATION SYSTEM");
        System.out.println("======================================");

        boolean running = true;

        while (running) {
            displayMenu();

            String choice = scanner.nextLine().trim();

            try {
            	switch (choice) {
	                case "1" -> addRoom();
	                case "2" -> viewRooms();
	                case "3" -> registerCustomer();
	                case "4" -> searchAvailableRooms();
	                case "5" -> bookRoom();
	                case "6" -> viewReservations();
	                case "7" -> cancelReservation();
	                case "8" -> running = false;
	                default -> System.out.println("Invalid choice.");
	            }
            } catch (Exception e) {
                System.out.println("\nOperation failed: " + e.getMessage());
            }
        }

        System.out.println("Thank you for using Hotel Reservation System!");
        scanner.close();
    }

    private static void displayMenu() {
        System.out.println("\n--------------- MENU ---------------");
        System.out.println("1. Add Room");
        System.out.println("2. View All Rooms");
        System.out.println("3. Register Customer");
        System.out.println("4. Search Available Rooms");
        System.out.println("5. Book Room");
        System.out.println("6. View Reservations");
        System.out.println("7. Cancel Reservation");
        System.out.println("8. Exit");
        System.out.print("Enter your choice: ");
    }

    private static void addRoom() throws SQLException {

        System.out.print("Room number: ");
        int roomNumber = Integer.parseInt(scanner.nextLine());

        System.out.println("Room types: STANDARD, DELUXE, SUITE");
        System.out.print("Room type: ");
        RoomType roomType = RoomType.valueOf(
                scanner.nextLine().trim().toUpperCase());

        System.out.print("Price per night: ");
        BigDecimal price = new BigDecimal(scanner.nextLine());

        System.out.print("Room capacity: ");
        int capacity = Integer.parseInt(scanner.nextLine());

        Room room = new Room(roomNumber, roomType, price, capacity);

        roomService.addRoom(room);

        System.out.println("Room added successfully!");
    }

    private static void viewRooms() throws SQLException {

        List<Room> rooms = roomService.getAllRooms();

        if (rooms.isEmpty()) {
            System.out.println("No rooms available in the system.");
            return;
        }

        System.out.println("\n------------- ROOMS -------------");

        for (Room room : rooms) {
            room.displayRoomDetails();
        }
    }

    private static void registerCustomer() throws SQLException {

        Customer customer = readCustomerDetails();

        Customer existing = customerService.findCustomerByEmail(
                customer.getEmail());

        if (existing != null) {
            System.out.println("Customer already registered.");
            return;
        }

        int customerId = customerService.registerCustomer(customer);

        System.out.println("Customer registered successfully!");
        System.out.println("Customer ID: " + customerId);
    }

    private static Customer readCustomerDetails() {

        System.out.print("Customer name: ");
        String name = scanner.nextLine();

        System.out.print("Email: ");
        String email = scanner.nextLine();

        System.out.print("Phone: ");
        String phone = scanner.nextLine();

        return new Customer(name, email, phone);
    }
    
    private static void searchAvailableRooms() throws SQLException {

        System.out.println("\n------ SEARCH AVAILABLE ROOMS ------");

        System.out.print("Check-in date (YYYY-MM-DD): ");
        LocalDate checkIn = LocalDate.parse(scanner.nextLine());

        System.out.print("Check-out date (YYYY-MM-DD): ");
        LocalDate checkOut = LocalDate.parse(scanner.nextLine());

        List<Room> availableRooms =
                roomService.findAvailableRooms(checkIn, checkOut);

        if (availableRooms.isEmpty()) {
            System.out.println("No rooms available for these dates.");
            return;
        }

        System.out.println("\nAvailable Rooms:");

        for (Room room : availableRooms) {
            room.displayRoomDetails();
        }
    }

    private static void bookRoom() throws SQLException {

        System.out.println("\n------------- ROOM BOOKING -------------");

        viewRooms();

        System.out.print("\nEnter room number: ");
        int roomNumber = Integer.parseInt(scanner.nextLine());

        Room room = roomService.findRoom(roomNumber);

        if (room == null) {
            System.out.println("Room not found.");
            return;
        }

        System.out.print("Customer email: ");
        String email = scanner.nextLine().trim();

        Customer customer = customerService.findCustomerByEmail(email);

        if (customer == null) {
            System.out.println("Customer is not registered. Enter details:");

            System.out.print("Name: ");
            String name = scanner.nextLine();

            System.out.print("Phone: ");
            String phone = scanner.nextLine();

            customer = new Customer(name, email, phone);

            int customerId = customerService.registerCustomer(customer);

            System.out.println("Customer registered. ID: " + customerId);
        }

        System.out.print("Check-in date (YYYY-MM-DD): ");
        LocalDate checkIn = LocalDate.parse(scanner.nextLine());

        System.out.print("Check-out date (YYYY-MM-DD): ");
        LocalDate checkOut = LocalDate.parse(scanner.nextLine());

        Reservation reservation = reservationService.bookRoom(
                customer, room, checkIn, checkOut);

        System.out.println("\nReservation created successfully!");
        reservation.displayReservationDetails();

        System.out.println("\nProcessing simulated payment...");

        Payment payment = paymentService.makePayment(reservation);

        System.out.println("Payment completed successfully!");
        payment.displayPaymentDetails();
    }

    private static void viewReservations() throws SQLException {

        List<Reservation> reservations =
                reservationService.getAllReservations();

        if (reservations.isEmpty()) {
            System.out.println("No reservations found.");
            return;
        }

        System.out.println("\n------------- RESERVATIONS -------------");

        for (Reservation reservation : reservations) {
            reservation.displayReservationDetails();
        }
    }

    private static void cancelReservation() throws SQLException {

        System.out.print("Enter reservation ID: ");

        int reservationId = Integer.parseInt(scanner.nextLine());

        reservationService.cancelReservation(reservationId);

        paymentService.refundForReservation(reservationId);

        System.out.println("Reservation cancelled successfully.");
        System.out.println("Any completed payment has been marked as refunded.");
    }
}