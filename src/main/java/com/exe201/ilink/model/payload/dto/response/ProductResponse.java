package com.exe201.ilink.model.payload.dto.response;

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
public class ProductResponse {
    @Schema(description = "Product's id", example = "1")
    @JsonProperty("product_id")
    private Long productId;

    @Schema(description = "Product's name", example = "Iphone 12")
    @JsonProperty("product_name")
    private String productName;

    @Schema(description = "Product's description", example = "The handmade gift for your loved ones.")
    private String description;

    @Schema(description = "Product's price", example = "1000")
    private Double price;

    @Schema(description = "Product's status", example = "ACTIVE")
    private String status;

    @Schema(description = "Product's image", example = "image.jpg")
    private String image;

    @Schema(description = "Product's stock", example = "10")
    private int stock;

    @Schema(description = "Product's category id", example = "Candle")
    @JsonProperty("category_name")
    private String categoryName;

    @Schema(description = "Product's shop id", example = "1")
    private Long shopId;

    @Schema(description = "Product's shop name", example = "Souvi")
    @JsonProperty("shop_name")
    private String shopName;


}
