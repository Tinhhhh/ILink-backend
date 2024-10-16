package com.exe201.ilink.controller;

import com.exe201.ilink.model.exception.ResponseBuilder;
import com.exe201.ilink.model.payload.request.OrderInfo;
import com.exe201.ilink.service.CustomerOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("order")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Method for order settings required access token to gain access")
public class CustomerOrderController {
    private final CustomerOrderService customerOrderService;

    @Operation(
        summary = "pay for the order in the shop and save order infor for buyer")
    @PostMapping("/saved-order")
    public ResponseEntity<Object> getOrderDetails(@RequestBody OrderInfo orderInfo
    ) {
//        customerOrderService.saveOrder(orderInfo);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Successfully stored information");
    }

    //view order history for buyer

    //view order history for seller

    //view order details for admin


}
