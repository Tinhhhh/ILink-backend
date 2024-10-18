package com.exe201.ilink.model.payload.request;

import com.exe201.ilink.model.payload.dto.OrderProductDTO;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfo {

    @JsonProperty("account_id")
    private UUID accountId;

    @JsonProperty("customer_name")
    private String customerName;

    @JsonProperty("shipped_address")
    private String address;

    private String description;

    @NotEmpty(message = "Phone cannot be blank")
    @Pattern(regexp = "^(84|0[3|5|7|8|9])[0-9]{8}$", message = "Please enter a valid(+84) phone number")
    private String phone;

    private List<OrderProductDTO> products;

    @JsonProperty("total_price")
    private int totalPrice;

    @JsonProperty("payment_date")
    private Date paymentDate;

}
