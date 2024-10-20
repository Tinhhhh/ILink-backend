package com.exe201.ilink.service;


import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.payload.request.OrderInfo;
import com.exe201.ilink.model.payload.response.OrderHistoryResponse;
import com.exe201.ilink.model.payload.response.PaymentStatementResponse;
import com.exe201.ilink.model.payload.response.RegistrationInfoResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Date;
import java.util.UUID;

public interface CustomerOrderService {
    void saveOrder(OrderInfo orderInfo, String orderCode);

    void updateOrder(String paymentId, String paymentCode, String paymentStatus, String orderCode, boolean cancel, HttpServletResponse response);

    OrderHistoryResponse getOrderDetailsForAdmin(int pageNo, int pageSize, ProductSort sortBy, String status, UUID sellerId, UUID buyerId, Date startDate, Date endDate);

    OrderHistoryResponse getOrderDetailsForBuyer(int pageNo, int pageSize, ProductSort sortBy, String status, UUID buyerId, Date startDate, Date endDate);

    OrderHistoryResponse getOrderDetailsForSeller(int pageNo, int pageSize, ProductSort sortBy, String status, UUID sellerId, Date startDate, Date endDate);

    RegistrationInfoResponse getRegistrationDetailsForAdmin(Date startDate, Date endDate);
}
