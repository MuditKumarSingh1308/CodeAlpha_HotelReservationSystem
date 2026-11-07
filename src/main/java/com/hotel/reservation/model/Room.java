package com.hotel.reservation.model;

import java.math.BigDecimal;
import java.util.Objects;

import com.hotel.reservation.enums.RoomType;

public class Room {
	
	private final int roomNumber;
	private final RoomType roomType;
	private final BigDecimal pricePerNight;
	private final int capacity;
	
	public Room(int roomNumber, RoomType roomType, BigDecimal pricePerNight, int capacity) {
		if(roomNumber <= 0) {
		    throw new IllegalArgumentException("Room number must be positive");
		}
		this.roomNumber = roomNumber;
		
		this.roomType = Objects.requireNonNull(roomType, "Room type cannot be null");
		
		BigDecimal validatedPrice = Objects.requireNonNull(pricePerNight, "Price per night cannot be null");
		if(validatedPrice.compareTo(BigDecimal.ZERO) <= 0) {
			throw new IllegalArgumentException("Price must be positive");
		}
		this.pricePerNight = validatedPrice;
		
		if(capacity <= 0) {
			throw new IllegalArgumentException("Room capacity must be positive");
		}
		this.capacity = capacity;
	}

	public int getRoomNumber() {
		return roomNumber;
	}
	
	public RoomType getRoomType() {
		return roomType;
	}
	
	public BigDecimal getPricePerNight() {
		return pricePerNight;
	}
	
	public int getCapacity() {
		return capacity;
	}
	
	public void displayRoomDetails() {
		System.out.println("Room Number: " + roomNumber);
		System.out.println("Room Type: " + roomType);
		System.out.println("Price Per Night: " + pricePerNight);
		System.out.println("Capacity: " + capacity);
	}
	
}
