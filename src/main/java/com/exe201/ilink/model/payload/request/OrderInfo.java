package com.exe201.ilink.model.payload.request;

import com.exe201.ilink.model.payload.dto.OrderProductDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfo {

    @JsonProperty("account_id")
    private UUID accountId;

    @JsonProperty("order_code")
    private String orderCode;

    @JsonProperty("shipped_address")
    private String address;

    private String status;

    private List<OrderProductDTO> products;

    @JsonProperty("total_price")
    private double totalPrice;

    @JsonProperty("payment_date")
    private Date paymentDate;

}
