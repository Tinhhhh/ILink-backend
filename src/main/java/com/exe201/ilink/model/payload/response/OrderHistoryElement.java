package com.exe201.ilink.model.payload.response;

import com.exe201.ilink.model.payload.dto.OrderProductDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class OrderHistoryElement {

    @JsonProperty("customer_order_id")
    private Long id;

    @JsonProperty("orderCode")
    private String orderCode;

    @JsonProperty("buyer_Id")
    private UUID buyerId;

    @JsonProperty("buyer_name")
    private String buyerName;

    @JsonProperty("customer_name")
    private String customerName;

    private String address;

    private String description;

    @JsonProperty("total_price")
    private int totalPrice;

    @JsonProperty("status")
    private String status;

    @JsonProperty("create_date")
    private String createdDate;

    @JsonProperty("product_list")
    private List<OrderProductDTO> productDTOList;

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("shop_name")
    private String shopName;

    @JsonProperty("payment_id")
    private String paymentId;

    @JsonProperty("payment_code")
    private String paymentCode;

    @JsonProperty("payment_method")
    private String paymentMethod;

}
