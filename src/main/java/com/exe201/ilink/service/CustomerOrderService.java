package com.exe201.ilink.service;


import com.exe201.ilink.model.payload.request.OrderInfo;
import com.exe201.ilink.model.payload.response.PaymentStatementResponse;

public interface CustomerOrderService {
    void saveOrder(OrderInfo orderInfo, String orderCode);
    PaymentStatementResponse updateOrder(String paymentId, String paymentCode, String paymentStatus, String orderCode, boolean cancel);
}
