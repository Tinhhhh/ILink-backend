package com.exe201.ilink.model.payload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderProductDTO {

    @JsonProperty("product_id")
    private Long productId;

    @JsonProperty("product_name")
    private String productName;

    private int quantity;

    @JsonProperty("unit_price")
    private int unitPrice;

    @JsonProperty("line_total")
    private int lineTotal;

}
