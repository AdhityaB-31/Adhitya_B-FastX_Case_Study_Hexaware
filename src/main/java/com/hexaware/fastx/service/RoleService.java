package com.hexaware.fastx.service;

import java.util.List;

import com.hexaware.fastx.dto.RoleDto;

public interface RoleService {
    RoleDto createRole(RoleDto roleDto);
    RoleDto updateRole(Long roleId, RoleDto roleDto);
    void deleteRole(Long roleId);
    RoleDto getRoleById(Long roleId);
    List<RoleDto> getAllRoles();
}
