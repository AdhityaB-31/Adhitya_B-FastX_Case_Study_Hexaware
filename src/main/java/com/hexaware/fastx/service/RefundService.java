package com.hexaware.fastx.service;

import java.util.List;

import com.hexaware.fastx.dto.RefundDto;

public interface RefundService {
    RefundDto createRefund(Long bookingId);
    RefundDto approveRefund(Long refundId);
    RefundDto getRefundStatus(Long refundId);
    List<RefundDto> getRefundsByStatus(com.hexaware.fastx.enums.RefundStatus status);
    List<RefundDto> getAllRefunds();
    RefundDto rejectRefund(Long refundId);
    RefundDto getRefundByBookingId(Long bookingId);
    List<RefundDto> getRefundsByOperator(Long operatorId);
    void verifyOperatorRefundOwnership(Long refundId, Long operatorId);
}
