package com.hotel.reservation.service;

import java.sql.SQLException;
import java.util.List;

import com.hotel.reservation.model.Customer;
import com.hotel.reservation.repository.CustomerRepository;

public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService() {
        this.customerRepository = new CustomerRepository();
    }

    public int registerCustomer(Customer customer) throws SQLException {

        if (customer == null) {
            throw new IllegalArgumentException("Customer cannot be null");
        }

        if (customerRepository.findByEmail(customer.getEmail()) != null) {
            throw new IllegalArgumentException(
                    "Customer with this email already exists");
        }

        return customerRepository.save(customer);
    }

    public Customer findCustomer(int customerId) throws SQLException {

        if (customerId <= 0) {
            throw new IllegalArgumentException("Invalid customer ID");
        }

        return customerRepository.findById(customerId);
    }

    public List<Customer> getAllCustomers() throws SQLException {
        return customerRepository.findAll();
    }
    
    public Customer findCustomerByEmail(String email) throws SQLException {
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }

        return customerRepository.findByEmail(email.trim());
    }
}