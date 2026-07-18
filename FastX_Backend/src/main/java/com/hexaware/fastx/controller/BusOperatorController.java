package com.hexaware.fastx.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.hexaware.fastx.dto.BusOperatorDto;
import com.hexaware.fastx.service.BusOperatorService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/operators")
public class BusOperatorController {

    @Autowired
    private BusOperatorService busOperatorService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<BusOperatorDto>> getAllOperators() {
        return new ResponseEntity<>(busOperatorService.getAllOperators(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/active")
    public ResponseEntity<List<BusOperatorDto>> getActiveOperators() {
        return new ResponseEntity<>(busOperatorService.getActiveOperators(), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/search")
    public ResponseEntity<List<BusOperatorDto>> searchOperatorsByName(@RequestParam String name) {
        return new ResponseEntity<>(busOperatorService.searchOperatorsByName(name), HttpStatus.OK);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete/{operatorId}")
    public ResponseEntity<Void> deleteOperator(@PathVariable Long operatorId) {
        busOperatorService.deleteOperator(operatorId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PreAuthorize("hasAnyRole('BUS_OPERATOR', 'ADMIN')")
    @GetMapping("/{operatorId}")
    public ResponseEntity<BusOperatorDto> getOperatorById(@PathVariable Long operatorId) {
        return new ResponseEntity<>(busOperatorService.getOperatorById(operatorId), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('BUS_OPERATOR', 'ADMIN')")
    @PutMapping("/update/{operatorId}")
    public ResponseEntity<BusOperatorDto> updateOperator(@PathVariable Long operatorId,
                                                          @Valid @RequestBody BusOperatorDto operatorDto) {
        return new ResponseEntity<>(busOperatorService.updateOperator(operatorId, operatorDto), HttpStatus.OK);
    }

    @PreAuthorize("hasAnyRole('BUS_OPERATOR', 'ADMIN')")
    @GetMapping("/email/{email}")
    public ResponseEntity<BusOperatorDto> getOperatorByEmail(@PathVariable String email) {
        return new ResponseEntity<>(busOperatorService.getOperatorByEmail(email), HttpStatus.OK);
    }
}
