package com.hexaware.fastx.service;

import java.util.List;

import com.hexaware.fastx.dto.BookingDto;
import com.hexaware.fastx.dto.GoogleUserInfo;
import com.hexaware.fastx.dto.UserDto;

public interface UserService {
    UserDto createUser(UserDto userDto);
    UserDto updateUser(Long userId, UserDto userDto);
    void deleteUser(Long userId);
    UserDto getUserById(Long userId);
    List<UserDto> getAllUsers();
    List<BookingDto> getBookingHistory(Long userId);
    UserDto getUserByEmail(String email);
    List<UserDto> searchUsersByName(String name);
    List<UserDto> getActiveUsers();

    /**
     * Finds an existing account by the Google-verified email, or creates a new
     * USER-role account for it. Returns the resulting user so the caller can
     * mint a JWT for it.
     */
    UserDto findOrCreateGoogleUser(GoogleUserInfo googleUserInfo);
}
