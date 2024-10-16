package com.exe201.ilink.controller;

import com.exe201.ilink.model.exception.ResponseBuilder;
import com.exe201.ilink.model.payload.response.PaymentStatementResponse;
import com.exe201.ilink.service.CustomerOrderService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import payment.CheckoutService;

@Controller
@Tag(name = "Checkout", description = "Method for checkout settings")
@RequiredArgsConstructor
public class CheckoutController {

    private final CheckoutService checkoutService;
    private final CustomerOrderService customerOrderService;

    @RequestMapping(value = {"/success", "/cancel"})
    public ResponseEntity<Object> Success(@RequestParam("id") String paymentId,
                                          @RequestParam("code") String paymentCode,
                                          @RequestParam("status") String paymentStatus,
                                          @RequestParam("orderCode") String orderCode,
                                          @RequestParam("cancel") boolean cancel) {

        PaymentStatementResponse object = PaymentStatementResponse.builder()
            .paymentId(paymentId)
            .paymentCode(paymentCode)
            .paymentStatus(paymentStatus)
            .orderCode(orderCode)
            .cancel(cancel)
            .build();

        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully status", object);
    }

    @RequestMapping(method = RequestMethod.POST, value = "/create-payment-link", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<Object> checkout(HttpServletRequest request) throws Exception {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully created payment link", checkoutService.checkout(request));
    }

}
