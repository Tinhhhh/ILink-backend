package com.exe201.ilink.model.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for new post information")
public class NewPostRequest {

    @Column(name = "title")
    private String title;

    @Schema(description = "Product's description", example = "The handmade gift for your loved ones.")
    private String description;

    @Schema(description = "Product's shop id", example = "1")
    private Long shopId;

    @Schema(description = "Product's id", example = "1")
    private List<Long> products;

}
