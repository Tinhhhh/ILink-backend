package com.exe201.ilink.model.payload.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for product information")
public class ProductRequest {

    @Schema(description = "Product's name", example = "Iphone 12")
    @JsonProperty("product_name")
    private String productName;

    @Schema(description = "Product's description", example = "The handmade gift for your loved ones.")
    private String description;

    @Schema(description = "Product's price", example = "1000")
    private double price;

    @Schema(description = "Product's image", example = "image.jpg")
    private String image;

    @Schema(description = "Product's stock", example = "10")
    private int stock;

    @Schema(description = "Product's category id", example = "1")
    private long categoryId;

    @Schema(description = "Product's shop id", example = "1")
    private long shopId;


}
