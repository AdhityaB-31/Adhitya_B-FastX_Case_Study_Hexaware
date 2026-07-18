package com.hexaware.fastx.service.impl;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.BusDto;
import com.hexaware.fastx.dto.SeatDto;
import com.hexaware.fastx.entity.Amenity;
import com.hexaware.fastx.entity.Bus;
import com.hexaware.fastx.entity.BusOperator;
import com.hexaware.fastx.entity.Seat;
import com.hexaware.fastx.enums.BusType;
import com.hexaware.fastx.enums.SeatStatus;
import com.hexaware.fastx.enums.SeatType;
import com.hexaware.fastx.exception.BusNotFoundException;
import com.hexaware.fastx.exception.ResourceNotFoundException;
import com.hexaware.fastx.exception.UnauthorizedException;
import com.hexaware.fastx.repository.AmenityRepository;
import com.hexaware.fastx.repository.BusOperatorRepository;
import com.hexaware.fastx.repository.BusRepository;
import com.hexaware.fastx.repository.SeatRepository;
import com.hexaware.fastx.service.BusService;

@Service
@Transactional
public class BusServiceImpl implements BusService {

	private static final Logger logger = LoggerFactory.getLogger(BusServiceImpl.class);

	@Autowired
	private BusRepository busRepository;

	@Autowired
	private BusOperatorRepository busOperatorRepository;

	@Autowired
	private AmenityRepository amenityRepository;

	@Autowired
	private SeatRepository seatRepository;

	@Override
	@Transactional
	public BusDto addBus(BusDto busDto) {
		Bus bus = mapToEntity(busDto);

		Bus savedBus = busRepository.save(bus);
		logger.info("Bus added with ID: {}", savedBus.getBusId());

		if (savedBus.getTotalSeats() != null && savedBus.getTotalSeats() > 0) {
			generateSeatsForBus(savedBus);
			logger.info("{} seats created for Bus ID: {}", savedBus.getTotalSeats(), savedBus.getBusId());
		}

		return mapToDto(savedBus);
	}

	@Override
	@Transactional
	public BusDto updateBus(Long busId, BusDto busDto) {
		Bus bus = busRepository.findById(busId)
				.orElseThrow(() -> new BusNotFoundException("Bus not found with ID: " + busId));
		bus.setBusName(busDto.getBusName());
		bus.setBusNumber(busDto.getBusNumber());
		bus.setBusType(BusType.valueOf(busDto.getBusType()));
		bus.setOrigin(busDto.getOrigin());
		bus.setDestination(busDto.getDestination());
		bus.setJourneyDate(busDto.getJourneyDate());
		bus.setDepartureTime(busDto.getDepartureTime());
		bus.setArrivalTime(busDto.getArrivalTime());
		bus.setFare(busDto.getFare());
		bus.setTotalSeats(busDto.getTotalSeats());

		if (busDto.getAmenityIds() != null) {
			List<Amenity> amenities = busDto.getAmenityIds().isEmpty()
					? new ArrayList<>()
					: amenityRepository.findAllById(busDto.getAmenityIds());
			bus.setAmenities(amenities);
		}

		Bus updatedBus = busRepository.save(bus);
		logger.info("Bus updated with ID: {}", updatedBus.getBusId());
		return mapToDto(updatedBus);
	}

	@Override
	public void deleteBus(Long busId) {
		Bus bus = busRepository.findById(busId)
				.orElseThrow(() -> new BusNotFoundException("Bus not found with ID: " + busId));
		busRepository.delete(bus);
		logger.info("Bus deleted with ID: {}", busId);
	}

	@Override
	public void verifyBusOwnership(Long busId, Long operatorId) {
		Bus bus = busRepository.findById(busId)
				.orElseThrow(() -> new BusNotFoundException("Bus not found with ID: " + busId));

		if (bus.getBusOperator() == null || !bus.getBusOperator().getOperatorId().equals(operatorId)) {
			logger.warn("Operator ID {} attempted to access Bus ID {} which they do not own", operatorId, busId);
			throw new UnauthorizedException(
					"You do not own Bus ID: " + busId + ". You can only manage your own buses.");
		}
	}

	@Override
	@Transactional(readOnly = true)
	public List<BusDto> searchBus(String origin, String destination, LocalDate journeyDate) {
		List<Bus> buses = busRepository.findByOriginAndDestinationAndJourneyDate(origin, destination, journeyDate);
		List<BusDto> busDtos = new ArrayList<>();
		LocalDate today = LocalDate.now();
		LocalTime now = LocalTime.now();

		for (Bus bus : buses) {
			if (bus.getJourneyDate() != null) {
				if (bus.getJourneyDate().isBefore(today) ||
						(bus.getJourneyDate().isEqual(today) && bus.getDepartureTime() != null
								&& bus.getDepartureTime().isBefore(now))) {
					continue;
				}
			}
			busDtos.add(mapToDto(bus));
		}

		return busDtos;
	}

	@Override
	@Transactional(readOnly = true)
	public BusDto getBusDetails(Long busId) {
		Bus bus = busRepository.findById(busId)
				.orElseThrow(() -> new BusNotFoundException("Bus not found with ID: " + busId));
		return mapToDto(bus);
	}

	@Override
	public List<SeatDto> getAvailableSeats(Long busId) {
		List<Seat> seats = seatRepository.findSeatsByBusIdAndStatus(busId, SeatStatus.AVAILABLE);
		List<SeatDto> seatDtos = new ArrayList<>();

		for (Seat seat : seats) {
			SeatDto seatDto = new SeatDto();
			seatDto.setSeatId(seat.getSeatId());
			seatDto.setSeatNumber(seat.getSeatNumber());
			seatDto.setSeatType(seat.getSeatType().name());
			seatDto.setSeatStatus(seat.getSeatStatus().name());
			seatDto.setBusId(seat.getBus().getBusId());
			seatDtos.add(seatDto);
		}

		return seatDtos;
	}

	@Override
	@Transactional(readOnly = true)
	public List<BusDto> getBusesByOperator(Long operatorId) {
		List<Bus> allBuses = busRepository.findAll();
		List<BusDto> operatorBuses = new ArrayList<>();

		for (Bus bus : allBuses) {
			if (bus.getBusOperator() != null && bus.getBusOperator().getOperatorId().equals(operatorId)) {
				operatorBuses.add(mapToDto(bus));
			}
		}

		return operatorBuses;
	}

	@Override
	public List<BusDto> getBusesByType(BusType busType) {

		List<Bus> buses = busRepository.findByBusType(busType);
		List<BusDto> busDtos = new ArrayList<>();

		for (Bus bus : buses) {
			busDtos.add(mapToDto(bus));
		}

		return busDtos;
	}

	@Override
	public List<BusDto> getBusesByFare(Double minFare, Double maxFare) {

		List<Bus> buses = busRepository.findByFareBetween(
				minFare,
				maxFare);

		List<BusDto> busDtos = new ArrayList<>();

		for (Bus bus : buses) {
			busDtos.add(mapToDto(bus));
		}

		return busDtos;
	}

	@Override
	public List<BusDto> searchBusesByName(String name) {

		List<Bus> buses = busRepository.findByBusNameContainingIgnoreCase(name);

		List<BusDto> busDtos = new ArrayList<>();

		for (Bus bus : buses) {
			busDtos.add(mapToDto(bus));
		}

		return busDtos;
	}

	@Override
	public List<BusDto> getCheapestBuses() {

		List<Bus> buses = busRepository.findTop5ByOrderByFareAsc();

		List<BusDto> busDtos = new ArrayList<>();

		for (Bus bus : buses) {
			busDtos.add(mapToDto(bus));
		}

		return busDtos;
	}

	@Override
	@Transactional(readOnly = true)
	public List<BusDto> getAllBuses() {
		List<Bus> buses = busRepository.findAll();
		List<BusDto> busDtos = new ArrayList<>();
		for (Bus bus : buses) {
			busDtos.add(mapToDto(bus));
		}
		return busDtos;
	}

	/**
	 * Auto-generates Seat entities for a newly created bus. Seat numbers follow the
	 * format: B{busNumber}-{sequenceNumber} (e.g. B-KA01-01, B-KA01-02 ...)
	 */
	private void generateSeatsForBus(Bus bus) {
		List<SeatType> seatTypePattern = getSeatTypePattern(bus.getBusType());
		List<Seat> seats = new ArrayList<>();

		for (int i = 1; i <= bus.getTotalSeats(); i++) {
			SeatType seatType = seatTypePattern.get((i - 1) % seatTypePattern.size());

			String sequenceNumber = String.format("%02d", i);

			String busNumberPart = String.valueOf(bus.getBusId());
			String seatNumber = "B" + busNumberPart + "-" + sequenceNumber;

			Seat seat = new Seat();
			seat.setSeatNumber(seatNumber);
			seat.setSeatType(seatType);
			seat.setSeatStatus(SeatStatus.AVAILABLE);
			seat.setBus(bus);

			seatRepository.save(seat);
			seats.add(seat);
		}
	}

	/**
	 * Returns a repeating pattern of seat types based on the bus type.
	 *
	 * Sleeper layout (4-berth unit): LB, UB, LB, UB, SL, SU → 40% Lower Berth, 40%
	 * Upper Berth, 10% Side Lower, 10% Side Upper
	 *
	 * Seater layout (3+2 row): WS, AS, MS, AS, WS → 40% Window, 40% Aisle, 20%
	 * Middle
	 *
	 * Volvo layout (2+2 row): WS, AS, AS, WS → 50% Window, 50% Aisle
	 */
	private List<SeatType> getSeatTypePattern(BusType busType) {
		List<SeatType> pattern = new ArrayList<>();

		if (busType == null) {
			pattern.add(SeatType.WINDOW_SEAT);
			pattern.add(SeatType.AISLE_SEAT);
			return pattern;
		}

		if (busType == BusType.AC_SLEEPER || busType == BusType.NON_AC_SLEEPER) {
			pattern.add(SeatType.LP);
			pattern.add(SeatType.UP);
		} else {
			pattern.add(SeatType.SE);
		}

		return pattern;
	}

	private BusDto mapToDto(Bus bus) {
		BusDto dto = new BusDto();
		dto.setBusId(bus.getBusId());
		dto.setBusName(bus.getBusName());
		dto.setBusNumber(bus.getBusNumber());
		dto.setBusType(bus.getBusType() != null ? bus.getBusType().name() : null);
		dto.setOrigin(bus.getOrigin());
		dto.setDestination(bus.getDestination());
		dto.setJourneyDate(bus.getJourneyDate());
		dto.setDepartureTime(bus.getDepartureTime());
		dto.setArrivalTime(bus.getArrivalTime());
		dto.setFare(bus.getFare());
		dto.setTotalSeats(bus.getTotalSeats());
		dto.setOperatorId(bus.getBusOperator() != null ? bus.getBusOperator().getOperatorId() : null);

		if (bus.getAmenities() != null) {
			List<Long> amenityIds = new ArrayList<>();
			List<String> amenityNames = new ArrayList<>();
			for (Amenity amenity : bus.getAmenities()) {
				amenityIds.add(amenity.getAmenityId());
				amenityNames.add(amenity.getAmenityName());
			}
			dto.setAmenityIds(amenityIds);
			dto.setAmenityNames(amenityNames);
		}

		return dto;
	}

	private Bus mapToEntity(BusDto dto) {
		Bus bus = new Bus();
		bus.setBusName(dto.getBusName());
		bus.setBusNumber(dto.getBusNumber());
		if (dto.getBusType() != null) {
			bus.setBusType(BusType.valueOf(dto.getBusType()));
		}
		bus.setOrigin(dto.getOrigin());
		bus.setDestination(dto.getDestination());
		bus.setJourneyDate(dto.getJourneyDate());
		bus.setDepartureTime(dto.getDepartureTime());
		bus.setArrivalTime(dto.getArrivalTime());
		bus.setFare(dto.getFare());
		bus.setTotalSeats(dto.getTotalSeats());

		if (dto.getOperatorId() != null) {
			BusOperator operator = busOperatorRepository.findById(dto.getOperatorId()).orElseThrow(
					() -> new ResourceNotFoundException("Operator not found with ID: " + dto.getOperatorId()));
			bus.setBusOperator(operator);
		}

		if (dto.getAmenityIds() != null && !dto.getAmenityIds().isEmpty()) {
			List<Amenity> amenities = amenityRepository.findAllById(dto.getAmenityIds());
			bus.setAmenities(amenities);
		}

		return bus;
	}

}
