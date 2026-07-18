package com.hexaware.fastx.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hexaware.fastx.entity.Amenity;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {

    Optional<Amenity> findByAmenityName(String amenityName);

    List<Amenity> findByAmenityNameContainingIgnoreCase(String amenityName);

    boolean existsByAmenityName(String amenityName);
}
