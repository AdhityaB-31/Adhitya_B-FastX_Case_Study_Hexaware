package com.hexaware.fastx.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.RoleDto;
import com.hexaware.fastx.dto.UserDto;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceTest {

        @Autowired
        private UserService userService;

        @Autowired
        private RoleService roleService;

        // Create role for user testing
        private Long createRole() {

                RoleDto role = new RoleDto();
                role.setRoleName("ROLE_USER");

                return roleService
                                .createRole(role)
                                .getRoleId();
        }

        @Test
        void testCreateUser() {

                Long roleId = createRole();

                UserDto user = new UserDto();
                user.setFullName("Adhitya");
                user.setEmail("adhitya" + System.nanoTime() + "@fastx.com");
                user.setPassword("password123");
                user.setPhoneNumber("9876543210");
                user.setGender("MALE");
                user.setAddress("Puducherry");
                user.setIsActive(true);
                user.setRoleId(roleId);

                UserDto result = userService.createUser(user);

                assertNotNull(result);
                assertNotNull(result.getUserId());

                assertEquals(
                                "Adhitya",
                                result.getFullName());
        }

        @Test
        void testGetUserById() {

                Long roleId = createRole();

                UserDto user = new UserDto();
                user.setFullName("Buddy");
                user.setEmail("buddy" + System.nanoTime() + "@fastx.com");
                user.setPassword("password123");
                user.setPhoneNumber("9876543211");
                user.setGender("MALE");
                user.setAddress("Puducherry");
                user.setIsActive(true);
                user.setRoleId(roleId);

                UserDto savedUser = userService.createUser(user);

                UserDto result = userService.getUserById(
                                savedUser.getUserId());

                assertNotNull(result);

                assertEquals(
                                savedUser.getUserId(),
                                result.getUserId());
        }

        @Test
        void testGetAllUsers() {

                Long roleId = createRole();

                UserDto user = new UserDto();
                user.setFullName("Adhitya");
                user.setEmail("alluser" + System.nanoTime() + "@fastx.com");
                user.setPassword("password123");
                user.setPhoneNumber("9876543212");
                user.setGender("MALE");
                user.setAddress("Puducherry");
                user.setIsActive(true);
                user.setRoleId(roleId);

                userService.createUser(user);

                List<UserDto> users = userService.getAllUsers();

                assertNotNull(users);

                assertTrue(
                                users.size() > 0);
        }

        @Test
        void testUpdateUser() {

                Long roleId = createRole();

                UserDto user = new UserDto();
                user.setFullName("Adhitya");
                user.setEmail("update" + System.nanoTime() + "@fastx.com");
                user.setPassword("password123");
                user.setPhoneNumber("9876543213");
                user.setGender("MALE");
                user.setAddress("Puducherry");
                user.setIsActive(true);
                user.setRoleId(roleId);

                UserDto savedUser = userService.createUser(user);

                UserDto updatedUser = new UserDto();

                updatedUser.setFullName("Buddy");
                updatedUser.setPhoneNumber("9123456789");
                updatedUser.setGender("MALE");
                updatedUser.setAddress("Chennai");

                UserDto result = userService.updateUser(
                                savedUser.getUserId(),
                                updatedUser);

                assertNotNull(result);

                assertEquals(
                                "Buddy",
                                result.getFullName());

                assertEquals(
                                "Chennai",
                                result.getAddress());
        }

        @Test
        void testDeleteUser() {

                Long roleId = createRole();

                UserDto user = new UserDto();
                user.setFullName("Delete User");
                user.setEmail("delete" + System.nanoTime() + "@fastx.com");
                user.setPassword("password123");
                user.setPhoneNumber("9876543214");
                user.setGender("MALE");
                user.setAddress("Puducherry");
                user.setIsActive(true);
                user.setRoleId(roleId);

                UserDto savedUser = userService.createUser(user);

                userService.deleteUser(
                                savedUser.getUserId());

                assertThrows(
                                RuntimeException.class,
                                () -> userService.getUserById(
                                                savedUser.getUserId()));
        }

        @Test
        void testGetBookingHistory() {

                Long roleId = createRole();

                UserDto user = new UserDto();
                user.setFullName("Booking User");
                user.setEmail("booking" + System.nanoTime() + "@fastx.com");
                user.setPassword("password123");
                user.setPhoneNumber("9876543215");
                user.setGender("MALE");
                user.setAddress("Puducherry");
                user.setIsActive(true);
                user.setRoleId(roleId);

                UserDto savedUser = userService.createUser(user);

                // Just test that the method can be called without exception
                assertDoesNotThrow(() -> userService.getBookingHistory(savedUser.getUserId()));
        }

        @Test
        void testGetUserByEmail() {

                Long roleId = createRole();
                String email = "emailuser" + System.nanoTime() + "@fastx.com";

                UserDto user = new UserDto();
                user.setFullName("Email User");
                user.setEmail(email);
                user.setPassword("password123");
                user.setPhoneNumber("9876543216");
                user.setGender("MALE");
                user.setAddress("Puducherry");
                user.setIsActive(true);
                user.setRoleId(roleId);

                userService.createUser(user);

                UserDto result = userService.getUserByEmail(email);

                assertNotNull(result);
                assertEquals(email, result.getEmail());
        }

        @Test
        void testSearchUsersByName() {

                Long roleId = createRole();

                UserDto user = new UserDto();
                user.setFullName("Searchable Name");
                user.setEmail("searchuser" + System.nanoTime() + "@fastx.com");
                user.setPassword("password123");
                user.setPhoneNumber("9876543217");
                user.setGender("MALE");
                user.setAddress("Puducherry");
                user.setIsActive(true);
                user.setRoleId(roleId);

                userService.createUser(user);

                List<UserDto> results = userService.searchUsersByName("Searchable");

                assertNotNull(results);
                assertTrue(results.size() > 0);
        }

        @Test
        void testGetActiveUsers() {

                List<UserDto> activeUsers = userService.getActiveUsers();

                assertNotNull(activeUsers);
        }
}