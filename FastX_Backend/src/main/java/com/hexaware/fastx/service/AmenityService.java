package com.hexaware.fastx.service;

import java.util.List;

import com.hexaware.fastx.dto.AmenityDto;

public interface AmenityService {
    AmenityDto createAmenity(AmenityDto amenityDto);
    AmenityDto updateAmenity(Long amenityId, AmenityDto amenityDto);
    void deleteAmenity(Long amenityId);
    AmenityDto getAmenityById(Long amenityId);
    List<AmenityDto> getAllAmenities();
    AmenityDto getAmenityByName(String name);
    List<AmenityDto> searchAmenitiesByName(String name);
}
