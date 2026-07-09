package com.hexaware.fastx.service;

import java.util.List;

import com.hexaware.fastx.dto.BusOperatorDto;

public interface BusOperatorService {
    BusOperatorDto createOperator(BusOperatorDto operatorDto);
    BusOperatorDto updateOperator(Long operatorId, BusOperatorDto operatorDto);
    void deleteOperator(Long operatorId);
    BusOperatorDto getOperatorById(Long operatorId);
    List<BusOperatorDto> getAllOperators();
    BusOperatorDto getOperatorByEmail(String email);
    List<BusOperatorDto> getActiveOperators();
    List<BusOperatorDto> searchOperatorsByName(String name);
}
