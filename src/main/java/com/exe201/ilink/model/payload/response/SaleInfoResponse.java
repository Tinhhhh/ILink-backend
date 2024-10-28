package com.exe201.ilink.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SaleInfoResponse {

    @JsonProperty(value = "total_products", index = 1)
    private int totalProducts;

    @JsonProperty(value = "total_sales", index = 3)
    private int totalSales;

    @JsonProperty(value = "sale_percentage_changes", index = 4)
    private double salePercentageChanges;

    @JsonProperty(value = "total_net_sales", index = 5)
    private double totalNetSales;

    @JsonProperty(value = "net_sale_percentage_changes", index = 6)
    private double netSalePercentageChanges;

    @JsonProperty(value = "pending_products", index = 7)
    private int pendingProducts;

    @JsonProperty(value = "cancelled_products", index = 8)
    private int cancelledProducts;


}
