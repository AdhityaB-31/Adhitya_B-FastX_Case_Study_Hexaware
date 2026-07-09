package com.hexaware.fastx.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hexaware.fastx.dto.RoleDto;
import com.hexaware.fastx.service.RoleService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/roles")
@PreAuthorize("hasRole('ADMIN')")   // all endpoints in this controller = ADMIN only
public class RoleController {

    @Autowired
    private RoleService roleService;

    @PostMapping("/create")
    public ResponseEntity<RoleDto> createRole(@Valid @RequestBody RoleDto roleDto) {
        return new ResponseEntity<>(roleService.createRole(roleDto), HttpStatus.CREATED);
    }

    @PutMapping("/update/{roleId}")
    public ResponseEntity<RoleDto> updateRole(@PathVariable Long roleId, @Valid @RequestBody RoleDto roleDto) {
        return new ResponseEntity<>(roleService.updateRole(roleId, roleDto), HttpStatus.OK);
    }

    @DeleteMapping("/delete/{roleId}")
    public ResponseEntity<Void> deleteRole(@PathVariable Long roleId) {
        roleService.deleteRole(roleId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<RoleDto> getRoleById(@PathVariable Long roleId) {
        return new ResponseEntity<>(roleService.getRoleById(roleId), HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<RoleDto>> getAllRoles() {
        return new ResponseEntity<>(roleService.getAllRoles(), HttpStatus.OK);
    }
}
