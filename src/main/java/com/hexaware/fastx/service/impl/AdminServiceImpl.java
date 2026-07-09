package com.hexaware.fastx.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.BookingDto;
import com.hexaware.fastx.dto.BusOperatorDto;
import com.hexaware.fastx.dto.UserDto;
import com.hexaware.fastx.entity.Booking;
import com.hexaware.fastx.entity.BusOperator;
import com.hexaware.fastx.entity.User;
import com.hexaware.fastx.enums.BookingStatus;
import com.hexaware.fastx.repository.BookingRepository;
import com.hexaware.fastx.repository.BusOperatorRepository;
import com.hexaware.fastx.repository.UserRepository;
import com.hexaware.fastx.service.AdminService;

@Service
@Transactional
public class AdminServiceImpl implements AdminService {

    private static final Logger logger = LoggerFactory.getLogger(AdminServiceImpl.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BusOperatorRepository busOperatorRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private com.hexaware.fastx.service.EmailService emailService;

    @Override
    public Map<String, Object> getDashboardStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalUsers", userRepository.count());
        stats.put("totalOperators", busOperatorRepository.count());
        stats.put("totalBookings", bookingRepository.count());
        stats.put("confirmedBookings", bookingRepository.countByBookingStatus(BookingStatus.CONFIRMED));
        stats.put("cancelledBookings", bookingRepository.countByBookingStatus(BookingStatus.CANCELLED));
        stats.put("pendingBookings", bookingRepository.countByBookingStatus(BookingStatus.PENDING));
        stats.put("popularRoutes", bookingRepository.findPopularRoutes());
        logger.info("Admin dashboard statistics retrieved");
        return stats;
    }

    @Override
    public List<UserDto> manageUsers() {
        List<User> users = userRepository.findAll();
        List<UserDto> userDtos = new ArrayList<>();
        for (User user : users) {
            UserDto dto = new UserDto();
            dto.setUserId(user.getUserId());
            dto.setFullName(user.getFullName());
            dto.setEmail(user.getEmail());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setGender(user.getGender() != null ? user.getGender().name() : null);
            dto.setAddress(user.getAddress());
            dto.setIsActive(user.getIsActive());
            dto.setRoleId(user.getRole() != null ? user.getRole().getRoleId() : null);
            userDtos.add(dto);
        }
        return userDtos;
    }

    @Override
    public List<BusOperatorDto> manageOperators() {
        List<BusOperator> operators = busOperatorRepository.findAll();
        List<BusOperatorDto> operatorDtos = new ArrayList<>();
        for (BusOperator operator : operators) {
            BusOperatorDto dto = new BusOperatorDto();
            dto.setOperatorId(operator.getOperatorId());
            dto.setCompanyName(operator.getCompanyName());
            dto.setEmail(operator.getEmail());
            dto.setPhoneNumber(operator.getPhoneNumber());
            dto.setAddress(operator.getAddress());
            dto.setIsActive(operator.getIsActive());
            operatorDtos.add(dto);
        }
        return operatorDtos;
    }

    @Override
    public List<BookingDto> manageBookings() {
        List<Booking> bookings = bookingRepository.findAll();
        List<BookingDto> bookingDtos = new ArrayList<>();
        for (Booking booking : bookings) {
            BookingDto dto = new BookingDto();
            dto.setBookingId(booking.getBookingId());
            dto.setBookingDate(booking.getBookingDate());
            dto.setBookingStatus(booking.getBookingStatus() != null ? booking.getBookingStatus().name() : null);
            dto.setTotalAmount(booking.getTotalAmount());
            dto.setUserId(booking.getUser() != null ? booking.getUser().getUserId() : null);
            dto.setBusId(booking.getBus() != null ? booking.getBus().getBusId() : null);
            bookingDtos.add(dto);
        }
        return bookingDtos;
    }

    @Override
    public BusOperatorDto approveOperator(Long operatorId) {
        BusOperator operator = busOperatorRepository.findById(operatorId)
                .orElseThrow(() -> new com.hexaware.fastx.exception.ResourceNotFoundException("Operator not found with ID: " + operatorId));
        operator.setIsActive(true);
        BusOperator saved = busOperatorRepository.save(operator);
        
        emailService.sendApprovalEmail(saved.getEmail(), saved.getCompanyName());
        
        BusOperatorDto dto = new BusOperatorDto();
        dto.setOperatorId(saved.getOperatorId());
        dto.setCompanyName(saved.getCompanyName());
        dto.setEmail(saved.getEmail());
        dto.setPhoneNumber(saved.getPhoneNumber());
        dto.setAddress(saved.getAddress());
        dto.setIsActive(saved.getIsActive());
        return dto;
    }

    @Override
    public UserDto toggleUserStatus(Long userId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new com.hexaware.fastx.exception.ResourceNotFoundException("User not found with ID: " + userId));
        
        boolean willBeActive = !(user.getIsActive() != null && user.getIsActive());

        if (!willBeActive) {
            // Deactivating: check reason
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Reason is mandatory to deactivate a user.");
            }
            user.setIsActive(false);
            User saved = userRepository.save(user);
            emailService.sendDeactivationEmail(saved.getEmail(), saved.getFullName(), reason);
            
            UserDto dto = new UserDto();
            dto.setUserId(saved.getUserId());
            dto.setFullName(saved.getFullName());
            dto.setEmail(saved.getEmail());
            dto.setPhoneNumber(saved.getPhoneNumber());
            dto.setGender(saved.getGender() != null ? saved.getGender().name() : null);
            dto.setAddress(saved.getAddress());
            dto.setIsActive(saved.getIsActive());
            dto.setRoleId(saved.getRole() != null ? saved.getRole().getRoleId() : null);
            return dto;
        } else {
            // Activating
            user.setIsActive(true);
            User saved = userRepository.save(user);
            emailService.sendActivationEmail(saved.getEmail(), saved.getFullName());

            UserDto dto = new UserDto();
            dto.setUserId(saved.getUserId());
            dto.setFullName(saved.getFullName());
            dto.setEmail(saved.getEmail());
            dto.setPhoneNumber(saved.getPhoneNumber());
            dto.setGender(saved.getGender() != null ? saved.getGender().name() : null);
            dto.setAddress(saved.getAddress());
            dto.setIsActive(saved.getIsActive());
            dto.setRoleId(saved.getRole() != null ? saved.getRole().getRoleId() : null);
            return dto;
        }
    }

    @Override
    public BusOperatorDto toggleOperatorStatus(Long operatorId, String reason) {
        BusOperator operator = busOperatorRepository.findById(operatorId)
                .orElseThrow(() -> new com.hexaware.fastx.exception.ResourceNotFoundException("Operator not found with ID: " + operatorId));
        
        boolean willBeActive = !(operator.getIsActive() != null && operator.getIsActive());

        if (!willBeActive) {
            // Deactivating: check reason
            if (reason == null || reason.trim().isEmpty()) {
                throw new IllegalArgumentException("Reason is mandatory to deactivate an operator.");
            }
            operator.setIsActive(false);
            BusOperator saved = busOperatorRepository.save(operator);
            emailService.sendDeactivationEmail(saved.getEmail(), saved.getCompanyName(), reason);

            BusOperatorDto dto = new BusOperatorDto();
            dto.setOperatorId(saved.getOperatorId());
            dto.setCompanyName(saved.getCompanyName());
            dto.setEmail(saved.getEmail());
            dto.setPhoneNumber(saved.getPhoneNumber());
            dto.setAddress(saved.getAddress());
            dto.setIsActive(saved.getIsActive());
            return dto;
        } else {
            // Activating
            operator.setIsActive(true);
            BusOperator saved = busOperatorRepository.save(operator);
            emailService.sendActivationEmail(saved.getEmail(), saved.getCompanyName());

            BusOperatorDto dto = new BusOperatorDto();
            dto.setOperatorId(saved.getOperatorId());
            dto.setCompanyName(saved.getCompanyName());
            dto.setEmail(saved.getEmail());
            dto.setPhoneNumber(saved.getPhoneNumber());
            dto.setAddress(saved.getAddress());
            dto.setIsActive(saved.getIsActive());
            return dto;
        }
    }
}
