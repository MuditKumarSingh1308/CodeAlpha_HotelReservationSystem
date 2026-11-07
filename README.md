# Hotel Reservation System

A console-based Hotel Reservation System developed using Java, JDBC, Maven, and PostgreSQL. The application allows users to manage hotel rooms, register customers, search room availability, make reservations, simulate payments, and handle cancellations and refunds.

## Features

- **Room Management:** Add rooms with different categories, prices, and capacities.
- **Customer Management:** Register customers and retrieve existing customer details using email.
- **Room Availability Search:** Search for available rooms using check-in and check-out dates.
- **Reservation Management:** Create and view hotel reservations.
- **Double-Booking Prevention:** Prevent overlapping reservations for the same room.
- **Payment Simulation:** Process simulated payments and maintain payment records.
- **Cancellation and Refunds:** Cancel reservations and mark completed payments as refunded.
- **PostgreSQL Integration:** Persist application data using a relational database.

## Technology Stack

| Technology | Purpose |
|---|---|
| Java 25 | Core application development |
| PostgreSQL | Database management |
| JDBC | Database connectivity |
| Maven | Dependency and build management |
| JUnit 5 | Unit testing |
| Eclipse IDE | Development environment |

## Project Architecture

The application follows a layered architecture:

```text
com.hotel.reservation
│
├── enums
│   ├── RoomType
│   ├── ReservationStatus
│   └── PaymentStatus
│
├── model
│   ├── Room
│   ├── Customer
│   ├── Reservation
│   └── Payment
│
├── repository
│   ├── RoomRepository
│   ├── CustomerRepository
│   ├── ReservationRepository
│   └── PaymentRepository
│
├── service
│   ├── RoomService
│   ├── CustomerService
│   ├── ReservationService
│   └── PaymentService
│
├── ui
│   └── HotelReservationApp
│
└── util
    └── DatabaseConnection
```

### Architecture Responsibilities

- **Model Layer:** Represents rooms, customers, reservations, and payments.
- **Repository Layer:** Handles SQL queries and database operations using JDBC.
- **Service Layer:** Implements business logic and validation.
- **UI Layer:** Provides the interactive console menu.
- **Utility Layer:** Manages PostgreSQL database connections.

## Database Design

The application uses four relational tables:

| Table | Description |
|---|---|
| rooms | Stores room details, categories, prices, and capacities |
| customers | Stores customer information |
| reservations | Stores booking details and reservation status |
| payments | Stores payment amounts, dates, and payment status |

### Booking Availability Logic

The application prevents overlapping reservations using the following condition:

```sql
newCheckIn < existingCheckOut
AND
newCheckOut > existingCheckIn
```

Only confirmed reservations block room availability. Cancelled reservations do not prevent future bookings.

The reservation process also uses a database transaction and row locking to protect against concurrent bookings for the same room.

## Prerequisites

Install the following:

- JDK 25 or compatible Java environment
- Apache Maven 3.9+
- PostgreSQL
- Eclipse IDE or another Java IDE

## Database Setup

1. Open PostgreSQL using pgAdmin.
2. Create a database named:

```sql
CREATE DATABASE hotel_reservation_db;
```

3. Create the following tables:

```sql
CREATE TABLE rooms (
    room_id SERIAL PRIMARY KEY,
    room_number INT UNIQUE NOT NULL,
    room_type VARCHAR(20) NOT NULL
        CHECK (room_type IN ('STANDARD', 'DELUXE', 'SUITE')),
    price_per_night NUMERIC(10, 2) NOT NULL
        CHECK (price_per_night > 0),
    capacity INT NOT NULL CHECK (capacity > 0)
);

CREATE TABLE customers (
    customer_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL
);

CREATE TABLE reservations (
    reservation_id SERIAL PRIMARY KEY,
    customer_id INT NOT NULL REFERENCES customers(customer_id),
    room_id INT NOT NULL REFERENCES rooms(room_id),
    check_in DATE NOT NULL,
    check_out DATE NOT NULL,
    total_amount NUMERIC(10, 2) NOT NULL,
    reservation_status VARCHAR(20) NOT NULL
        CHECK (reservation_status IN ('CONFIRMED', 'CANCELLED')),
    CHECK (check_out > check_in),
    CHECK (total_amount > 0)
);

CREATE TABLE payments (
    payment_id SERIAL PRIMARY KEY,
    reservation_id INT NOT NULL REFERENCES reservations(reservation_id),
    amount NUMERIC(10, 2) NOT NULL CHECK (amount > 0),
    payment_status VARCHAR(20) NOT NULL
        CHECK (payment_status IN ('PENDING', 'COMPLETED', 'REFUNDED', 'FAILED')),
    payment_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_reservations_room_dates
ON reservations(room_id, check_in, check_out);

CREATE INDEX idx_reservations_customer
ON reservations(customer_id);
```

## Configuration

Create the file:

`src/main/resources/db.properties`

Add your PostgreSQL credentials:

```properties
db.url=jdbc:postgresql://localhost:5432/hotel_reservation_db
db.username=postgres
db.password=YOUR_POSTGRES_PASSWORD
```

Do not upload your actual database credentials to GitHub.

## Build and Run

Clone the repository:

```bash
git clone YOUR_GITHUB_REPOSITORY_URL
```

Navigate to the project directory:

```bash
cd hotel-reservation-system
```

Build the project:

```bash
mvn clean test
```

Run the main class:

`com.hotel.reservation.ui.HotelReservationApp`

In Eclipse, right-click `HotelReservationApp.java` and select:

**Run As → Java Application**

## Application Menu

```text
1. Add Room
2. View All Rooms
3. Register Customer
4. Search Available Rooms
5. Book Room
6. View Reservations
7. Cancel Reservation
8. Exit
```

## Payment Information

Payments are simulated for demonstration purposes. No actual financial transactions are processed.

## Future Improvements

- Graphical user interface or web-based interface.
- User authentication and role-based access.
- Integration with a real payment gateway.
- Automated email booking confirmations.
- Reservation history and reporting.
- Automated integration testing.

## Author

Developed as part of the CodeAlpha Java Programming Internship.

## License

This project is intended for educational and internship demonstration purposes.