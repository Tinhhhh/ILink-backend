package com.exe201.ilink.controller;

import com.exe201.ilink.model.exception.ResponseBuilder;
import com.exe201.ilink.model.payload.request.OrderInfo;
import com.exe201.ilink.service.CustomerOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import payment.CheckoutService;

@Tag(name = "Checkout", description = "Method for checkout settings")
@RestController
@RequestMapping("payment")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CustomerOrderService customerOrderService;

    @GetMapping(value = {"/success", "/cancel"})
    public ResponseEntity<Object> Success(@RequestParam("id") String paymentId,
                                          @RequestParam("code") String paymentCode,
                                          @RequestParam("status") String paymentStatus,
                                          @RequestParam("orderCode") String orderCode,
                                          @RequestParam("cancel") boolean cancel) {

        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully status", customerOrderService.updateOrder(paymentId, paymentCode, paymentStatus, orderCode, cancel));
    }

    //, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    @PostMapping(value = "/create-payment-link")
    public ResponseEntity<Object> checkout(HttpServletRequest request, @RequestBody OrderInfo orderInfo) throws Exception {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully created payment link", checkoutService.checkout(request, orderInfo));
    }

}
