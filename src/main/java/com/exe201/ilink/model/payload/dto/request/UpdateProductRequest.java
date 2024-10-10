package com.exe201.ilink.model.payload.dto.request;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    @Schema(description = "Product's name", example = "Iphone 12")
    @JsonProperty("product_name")
    private String productName;

    @Schema(description = "Product's description", example = "The handmade gift for your loved ones.")
    private String description;

    @Schema(description = "Product's price", example = "1000")
    private double price;

    @Schema(description = "Product's stock", example = "10")
    private int stock;

}
