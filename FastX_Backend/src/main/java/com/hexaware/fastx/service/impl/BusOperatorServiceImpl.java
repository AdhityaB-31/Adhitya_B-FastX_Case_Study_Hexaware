package com.hexaware.fastx.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hexaware.fastx.dto.BusOperatorDto;
import com.hexaware.fastx.entity.BusOperator;
import com.hexaware.fastx.exception.ResourceNotFoundException;
import com.hexaware.fastx.repository.BusOperatorRepository;
import com.hexaware.fastx.service.BusOperatorService;

@Service
@Transactional
public class BusOperatorServiceImpl implements BusOperatorService {

    private static final Logger logger = LoggerFactory.getLogger(BusOperatorServiceImpl.class);

    @Autowired
    private BusOperatorRepository busOperatorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public BusOperatorDto createOperator(BusOperatorDto operatorDto) {
        BusOperator operator = mapToEntity(operatorDto);

        operator.setPassword(passwordEncoder.encode(operatorDto.getPassword()));

        BusOperator savedOperator = busOperatorRepository.save(operator);
        logger.info("Bus Operator created with ID: {}", savedOperator.getOperatorId());
        return mapToDto(savedOperator);
    }

    @Override
    public BusOperatorDto updateOperator(Long operatorId, BusOperatorDto operatorDto) {
        BusOperator operator = busOperatorRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found with ID: " + operatorId));
        operator.setCompanyName(operatorDto.getCompanyName());
        operator.setPhoneNumber(operatorDto.getPhoneNumber());
        operator.setAddress(operatorDto.getAddress());

        if (operatorDto.getPassword() != null && !operatorDto.getPassword().isBlank()) {
            operator.setPassword(passwordEncoder.encode(operatorDto.getPassword()));
        }

        BusOperator updatedOperator = busOperatorRepository.save(operator);
        logger.info("Bus Operator updated with ID: {}", updatedOperator.getOperatorId());
        return mapToDto(updatedOperator);
    }

    @Override
    public void deleteOperator(Long operatorId) {
        BusOperator operator = busOperatorRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found with ID: " + operatorId));
        busOperatorRepository.delete(operator);
        logger.info("Bus Operator deleted with ID: {}", operatorId);
    }

    @Override
    public BusOperatorDto getOperatorById(Long operatorId) {
        BusOperator operator = busOperatorRepository.findById(operatorId)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found with ID: " + operatorId));
        return mapToDto(operator);
    }

    @Override
    public List<BusOperatorDto> getAllOperators() {
        List<BusOperator> operators = busOperatorRepository.findAll();
        List<BusOperatorDto> operatorDtos = new ArrayList<>();
        for (BusOperator operator : operators) {
            operatorDtos.add(mapToDto(operator));
        }
        return operatorDtos;
    }

    @Override
    public BusOperatorDto getOperatorByEmail(String email) {
        BusOperator operator = busOperatorRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Operator not found with email: " + email));
        return mapToDto(operator);
    }

    @Override
    public List<BusOperatorDto> getActiveOperators() {
        List<BusOperator> operators = busOperatorRepository.findByIsActiveTrue();
        List<BusOperatorDto> operatorDtos = new ArrayList<>();
        for (BusOperator operator : operators) {
            operatorDtos.add(mapToDto(operator));
        }
        return operatorDtos;
    }

    @Override
    public List<BusOperatorDto> searchOperatorsByName(String name) {
        List<BusOperator> operators = busOperatorRepository.findByCompanyNameContainingIgnoreCase(name);
        List<BusOperatorDto> operatorDtos = new ArrayList<>();
        for (BusOperator operator : operators) {
            operatorDtos.add(mapToDto(operator));
        }
        return operatorDtos;
    }

    private BusOperatorDto mapToDto(BusOperator operator) {
        BusOperatorDto dto = new BusOperatorDto();
        dto.setOperatorId(operator.getOperatorId());
        dto.setCompanyName(operator.getCompanyName());
        dto.setEmail(operator.getEmail());
        dto.setPhoneNumber(operator.getPhoneNumber());
        dto.setAddress(operator.getAddress());
        dto.setIsActive(operator.getIsActive());
        return dto;
    }

    private BusOperator mapToEntity(BusOperatorDto dto) {
        BusOperator operator = new BusOperator();
        operator.setCompanyName(dto.getCompanyName());
        operator.setEmail(dto.getEmail());
        operator.setPhoneNumber(dto.getPhoneNumber());
        operator.setAddress(dto.getAddress());
        operator.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : false);
        return operator;
    }
}
