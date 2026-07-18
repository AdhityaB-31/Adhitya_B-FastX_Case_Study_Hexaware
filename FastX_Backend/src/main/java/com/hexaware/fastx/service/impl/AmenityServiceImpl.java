package com.hexaware.fastx.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.AmenityDto;
import com.hexaware.fastx.entity.Amenity;
import com.hexaware.fastx.exception.ResourceNotFoundException;
import com.hexaware.fastx.repository.AmenityRepository;
import com.hexaware.fastx.service.AmenityService;
import java.util.stream.Collectors;

@Service
@Transactional
public class AmenityServiceImpl implements AmenityService {

	private static final Logger logger = LoggerFactory.getLogger(AmenityServiceImpl.class);

	@Autowired
	private AmenityRepository amenityRepository;

	@Override
	public AmenityDto createAmenity(AmenityDto amenityDto) {
		Amenity amenity = new Amenity();

		amenity.setAmenityName(amenityDto.getAmenityName());
		Amenity saved = amenityRepository.save(amenity);
		logger.info("Amenity created: {}", saved.getAmenityName());
		return mapToDto(saved);
	}

	@Override
	public AmenityDto updateAmenity(Long amenityId, AmenityDto amenityDto) {
		Amenity amenity = amenityRepository.findById(amenityId)
				.orElseThrow(() -> new ResourceNotFoundException("Amenity not found with ID: " + amenityId));
		amenity.setAmenityName(amenityDto.getAmenityName());
		Amenity updated = amenityRepository.save(amenity);
		return mapToDto(updated);
	}

	@Override
	public void deleteAmenity(Long amenityId) {
		Amenity amenity = amenityRepository.findById(amenityId)
				.orElseThrow(() -> new ResourceNotFoundException("Amenity not found with ID: " + amenityId));
		amenityRepository.delete(amenity);
		logger.info("Amenity deleted with ID: {}", amenityId);
	}

	@Override
	public AmenityDto getAmenityById(Long amenityId) {
		Amenity amenity = amenityRepository.findById(amenityId)
				.orElseThrow(() -> new ResourceNotFoundException("Amenity not found with ID: " + amenityId));
		return mapToDto(amenity);
	}

	@Override
	public List<AmenityDto> getAllAmenities() {
		List<Amenity> amenities = amenityRepository.findAll();
		List<AmenityDto> amenityDtos = new ArrayList<>();

		for (Amenity amenity : amenities) {
			amenityDtos.add(mapToDto(amenity));
		}

		return amenityDtos;
	}

	@Override
	public AmenityDto getAmenityByName(String name) {
		Amenity amenity = amenityRepository.findByAmenityName(name)
				.orElseThrow(() -> new ResourceNotFoundException("Amenity not found with name: " + name));
		return mapToDto(amenity);
	}

	@Override
	public List<AmenityDto> searchAmenitiesByName(String name) {
		return amenityRepository.findByAmenityNameContainingIgnoreCase(name).stream().map(this::mapToDto)
				.collect(Collectors.toList());
	}

	private AmenityDto mapToDto(Amenity amenity) {
		AmenityDto dto = new AmenityDto();
		dto.setAmenityId(amenity.getAmenityId());
		dto.setAmenityName(amenity.getAmenityName());
		return dto;
	}

}