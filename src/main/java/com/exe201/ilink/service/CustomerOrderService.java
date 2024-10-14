package com.exe201.ilink.service;


import com.exe201.ilink.model.payload.request.OrderInfo;

public interface CustomerOrderService {
    void saveOrder(OrderInfo orderInfo);
}
