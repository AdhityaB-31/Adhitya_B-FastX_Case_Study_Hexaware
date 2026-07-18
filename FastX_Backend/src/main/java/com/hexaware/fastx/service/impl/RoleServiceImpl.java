package com.hexaware.fastx.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.RoleDto;
import com.hexaware.fastx.entity.Role;
import com.hexaware.fastx.exception.ResourceNotFoundException;
import com.hexaware.fastx.repository.RoleRepository;
import com.hexaware.fastx.service.RoleService;

@Service
@Transactional
public class RoleServiceImpl implements RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleServiceImpl.class);

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public RoleDto createRole(RoleDto roleDto) {
        Role role = new Role();
        role.setRoleName(roleDto.getRoleName());
        Role savedRole = roleRepository.save(role);
        logger.info("Role created with ID: {}", savedRole.getRoleId());
        return mapToDto(savedRole);
    }

    @Override
    public RoleDto updateRole(Long roleId, RoleDto roleDto) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        role.setRoleName(roleDto.getRoleName());
        Role updatedRole = roleRepository.save(role);
        return mapToDto(updatedRole);
    }

    @Override
    public void deleteRole(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        roleRepository.delete(role);
    }

    @Override
    public RoleDto getRoleById(Long roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found with ID: " + roleId));
        return mapToDto(role);
    }

    @Override
    public List<RoleDto> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        List<RoleDto> roleDtos = new ArrayList<>();
        for (Role role : roles) {
            roleDtos.add(mapToDto(role));
        }
        return roleDtos;
    }

    private RoleDto mapToDto(Role role) {
        RoleDto dto = new RoleDto();
        dto.setRoleId(role.getRoleId());
        dto.setRoleName(role.getRoleName());
        return dto;
    }
}
