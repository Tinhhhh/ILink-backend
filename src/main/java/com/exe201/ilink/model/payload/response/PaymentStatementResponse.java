package com.exe201.ilink.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PaymentStatementResponse {

    @JsonProperty("payment_id")
    private String paymentId;
    @JsonProperty("payment_code")
    private String paymentCode;
    @JsonProperty("payment_date")
    private String paymentStatus;
    @JsonProperty("order_code")
    private String orderCode;
    private boolean cancel;
}
