package com.hexaware.fastx.service;

import java.util.List;
import java.util.Map;

import com.hexaware.fastx.dto.BookingDto;
import com.hexaware.fastx.dto.BusOperatorDto;
import com.hexaware.fastx.dto.UserDto;

public interface AdminService {
    Map<String, Object> getDashboardStatistics();
    List<UserDto> manageUsers();
    List<BusOperatorDto> manageOperators();
    List<BookingDto> manageBookings();
    BusOperatorDto approveOperator(Long operatorId);
    UserDto toggleUserStatus(Long userId, String reason);
    BusOperatorDto toggleOperatorStatus(Long operatorId, String reason);
}
