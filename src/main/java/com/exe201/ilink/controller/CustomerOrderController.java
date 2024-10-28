package com.exe201.ilink.controller;

import com.exe201.ilink.Util.AppConstants;
import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.exception.ResponseBuilder;
import com.exe201.ilink.service.CustomerOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.UUID;

@RestController
@RequestMapping("order")
@RequiredArgsConstructor
@Tag(name = "Order", description = "Method for order settings required access token to gain access")
public class CustomerOrderController {
    private final CustomerOrderService customerOrderService;

    @Operation(
        summary = "Get order details for admin")
    @GetMapping("/admin/details")
    public ResponseEntity<Object> getOrderDetailForAdmin(@RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                                         @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                                         @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_PRODUCT_SORT_DATE_DSC, required = false) ProductSort sortBy,
                                                         @RequestParam(name = "status", required = false) String status,
                                                         @RequestParam(name = "sellerId", required = false) UUID sellerId,
                                                         @RequestParam(name = "buyerId", required = false) UUID buyerId,
                                                         @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                         @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved order", customerOrderService.getOrderDetailsForAdmin(pageNo, pageSize, sortBy, status, sellerId, buyerId, startDate, endDate));
    }

    @Operation(
        summary = "Get order details for seller")
    @GetMapping("/seller/details")
    public ResponseEntity<Object> getOrderDetailForSeller(@RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                                          @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                                          @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_PRODUCT_SORT_DATE_DSC, required = false) ProductSort sortBy,
                                                          @RequestParam(name = "status", required = false) String status,
                                                          @RequestParam(name = "sellerId") UUID sellerId,
                                                          @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                          @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved order", customerOrderService.getOrderDetailsForSeller(pageNo, pageSize, sortBy, status, sellerId, startDate, endDate));
    }

    @Operation(
        summary = "Get order details for buyer")
    @GetMapping("/buyer/details")
    public ResponseEntity<Object> getOrderDetailForBuyer(@RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                                         @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                                         @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_PRODUCT_SORT_DATE_DSC, required = false) ProductSort sortBy,
                                                         @RequestParam(name = "status", required = false) String status,
                                                         @RequestParam(name = "buyerId") UUID buyerId,
                                                         @RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                         @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved order", customerOrderService.getOrderDetailsForBuyer(pageNo, pageSize, sortBy, status, buyerId, startDate, endDate));
    }

    @Operation(
        summary = "Get registration details details for admin")
    @GetMapping("/admin/registration-details")
    public ResponseEntity<Object> getRegistrationDetailsForAdmin(@RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                                 @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved registration details", customerOrderService.getRegistrationDetailsForAdmin(startDate, endDate));
    }

    @Operation(
        summary = "Get sales details for seller")
    @GetMapping("/seller/sale-details")
    public ResponseEntity<Object> getSalesDetailsForSeller(@RequestParam(name = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date startDate,
                                                           @RequestParam(name = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") Date endDate,
                                                           @RequestParam(name = "sellerId") UUID sellerId

    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved sale details", customerOrderService.getSalesDetailsForSeller(startDate, endDate, sellerId));
    }


}
