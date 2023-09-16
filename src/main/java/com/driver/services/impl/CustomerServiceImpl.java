package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.model.Driver;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;
import com.driver.model.TripStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function

		Optional<Customer> customerOptional = customerRepository2.findById(customerId);
		if(!customerOptional.isPresent()) {
			return;
		}
		Customer customer = customerOptional.get();
		customerRepository2.delete(customer);

	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query

		List<Driver> drivers = driverRepository2.findAll();

		Driver currDriver = null;
		for(Driver driver : drivers) {
			if(driver.getCab().isAvailable()) {
				currDriver = driver;
				break;
			}
		}
		if(currDriver == null) {
			throw new Exception("No cab available!");
		}

		Optional<Customer> customerOptional = customerRepository2.findById(customerId);

		if(!customerOptional.isPresent()) {
			throw new Exception("Customer is not present!");
		}

		currDriver.getCab().setAvailable(false);
		Customer currCustomer = customerOptional.get();

		TripBooking tripBooking = new TripBooking();
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		tripBooking.setDistanceInKm(distanceInKm);
		tripBooking.setBill(0);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setDriver(currDriver);
		tripBooking.setCustomer(currCustomer);

		List<TripBooking> tripBookings = currDriver.getTripBookingList();
		tripBookings.add(tripBooking);

		List<TripBooking> tripBookings1 = currCustomer.getTripBookingList();
		tripBookings1.add(tripBooking);

		tripBookingRepository2.save(tripBooking);
		driverRepository2.save(currDriver);
		customerRepository2.save(currCustomer);

		return tripBooking;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		Optional<TripBooking> tripBookingOptional = tripBookingRepository2.findById(tripId);

		if(!tripBookingOptional.isPresent()){
			return;
		}

		TripBooking tripBooking = tripBookingOptional.get();
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setBill(0);
		tripBooking.setStatus(TripStatus.CANCELED);

		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly

		Optional<TripBooking> tripBookingOptional = tripBookingRepository2.findById(tripId);

		if(!tripBookingOptional.isPresent()){
			return;
		}

		TripBooking tripBooking = tripBookingOptional.get();
		tripBooking.setBill(tripBooking.getDriver().getCab().getPerKmRate() * tripBooking.getDistanceInKm());
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setStatus(TripStatus.COMPLETED);

		tripBookingRepository2.save(tripBooking);
	}
}
