package com.hexaware.fastx.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.RoleDto;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RoleServiceTest {

    @Autowired
    private RoleService roleService;

    @Test
    void testCreateRole() {

        RoleDto role = new RoleDto();
        role.setRoleName("ROLE_USER");

        RoleDto savedRole = roleService.createRole(role);

        assertNotNull(savedRole);
        assertNotNull(savedRole.getRoleId());

        assertEquals(
                "ROLE_USER",
                savedRole.getRoleName());
    }

    @Test
    void testGetRoleById() {

        RoleDto role = new RoleDto();
        role.setRoleName("ROLE_OPERATOR");

        RoleDto savedRole = roleService.createRole(role);

        RoleDto fetchedRole = roleService.getRoleById(
                savedRole.getRoleId());

        assertNotNull(fetchedRole);

        assertEquals(
                savedRole.getRoleId(),
                fetchedRole.getRoleId());

        assertEquals(
                "ROLE_OPERATOR",
                fetchedRole.getRoleName());
    }

    @Test
    void testGetAllRoles() {

        RoleDto role1 = new RoleDto();
        role1.setRoleName("ROLE_USER");
        roleService.createRole(role1);

        RoleDto role2 = new RoleDto();
        role2.setRoleName("ROLE_OPERATOR");
        roleService.createRole(role2);

        List<RoleDto> roles = roleService.getAllRoles();

        assertNotNull(roles);

        assertTrue(
                roles.size() >= 2);
    }

    @Test
    void testUpdateRole() {

        RoleDto role = new RoleDto();
        role.setRoleName("ROLE_USER");

        RoleDto savedRole = roleService.createRole(role);

        RoleDto updatedRole = new RoleDto();

        updatedRole.setRoleName(
                "ROLE_ADMIN");

        RoleDto result = roleService.updateRole(
                savedRole.getRoleId(),
                updatedRole);

        assertNotNull(result);

        assertEquals(
                "ROLE_ADMIN",
                result.getRoleName());
    }

    @Test
    void testDeleteRole() {

        RoleDto role = new RoleDto();
        role.setRoleName("ROLE_TEMP");

        RoleDto savedRole = roleService.createRole(role);

        roleService.deleteRole(
                savedRole.getRoleId());

        assertThrows(
                RuntimeException.class,
                () -> roleService.getRoleById(
                        savedRole.getRoleId()));
    }
}