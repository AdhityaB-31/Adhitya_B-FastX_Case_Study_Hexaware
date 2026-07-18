package com.hexaware.fastx.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hexaware.fastx.entity.Bus;
import com.hexaware.fastx.enums.BusType;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {

	List<Bus> findByOriginAndDestinationAndJourneyDate(String origin, String destination, LocalDate journeyDate);

	List<Bus> findByBusType(BusType busType);

	List<Bus> findByFareBetween(Double minFare, Double maxFare);

	List<Bus> findByBusNameContainingIgnoreCase(String busName);

	List<Bus> findTop5ByOrderByFareAsc();

	/*
	 * @Query(value = """ SELECT bus_id, SUM(total_amount) FROM bookings GROUP BY
	 * bus_id ORDER BY SUM(total_amount) DESC LIMIT 5 """, nativeQuery = true)
	 * 
	 * List<Object[]> getTopRevenueBuses();
	 */}
