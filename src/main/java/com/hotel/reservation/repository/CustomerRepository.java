package com.hotel.reservation.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.hotel.reservation.model.Customer;
import com.hotel.reservation.util.DatabaseConnection;

public class CustomerRepository {

    // Save customer
    public int save(Customer customer) throws SQLException {

        String sql = """
                INSERT INTO customers (name, email, phone)
                VALUES (?, ?, ?)
                RETURNING customer_id
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, customer.getName());
            statement.setString(2, customer.getEmail());
            statement.setString(3, customer.getPhone());

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    return result.getInt("customer_id");
                }
            }
        }

        throw new SQLException("Customer registration failed");
    }

    // Find customer by ID
    public Customer findById(int customerId) throws SQLException {

        String sql = "SELECT * FROM customers WHERE customer_id = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, customerId);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapCustomer(result);
                }
            }
        }

        return null;
    }

    // Find customer by email
    public Customer findByEmail(String email) throws SQLException {

        String sql = "SELECT * FROM customers WHERE email = ?";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, email);

            try (ResultSet result = statement.executeQuery()) {

                if (result.next()) {
                    return mapCustomer(result);
                }
            }
        }

        return null;
    }

    // Retrieve all customers
    public List<Customer> findAll() throws SQLException {

        List<Customer> customers = new ArrayList<>();

        String sql = "SELECT * FROM customers ORDER BY customer_id";

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet result = statement.executeQuery()) {

            while (result.next()) {
                customers.add(mapCustomer(result));
            }
        }

        return customers;
    }

    // Convert database record into Customer object
    private Customer mapCustomer(ResultSet result) throws SQLException {

        return new Customer(
                result.getString("name"),
                result.getString("email"),
                result.getString("phone")
        );
    }
}