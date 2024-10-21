package com.exe201.ilink.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationInfoResponse {

    @JsonProperty(value = "total_products", index = 1)
    private int totalProducts;

    @JsonProperty(value = "product_percentage_changes", index = 2)
    private double productPercentageChanges;

    @JsonProperty(value = "total_sales", index = 3)
    private int totalSales;

    @JsonProperty(value = "sale_percentage_changes", index = 4)
    private double salePercentageChanges;

    @JsonProperty(value = "total_customers", index = 5)
    private int totalCustomers;

    @JsonProperty(value = "customers_percentage_changes", index = 6)
    private double customersPercentageChanges;

    @JsonProperty(value = "total_transactions", index = 7)
    private int totalTransactions;

    @JsonProperty(value = "transaction_percentage_changes", index = 8)
    private double transactionPercentageChanges;

    @JsonProperty(value = "total_commission", index = 9)
    private double totalCommission;

    @JsonProperty(value = "commission_percentage_changes", index = 10)
    private double commissionPercentageChanges;


}
