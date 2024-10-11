package com.exe201.ilink.model.payload.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "Response object for post")
public class PostResponse {

    @Schema(description = "Post's id", example = "1")
    private Long id;

    @Column(name = "title")
    private String title;

    @Schema(description = "Product's description", example = "The handmade gift for your loved ones.")
    private String description;

    @Schema(description = "Post's status", example = "ACTIVE")
    private String status;

    private List<ProductResponse> products;
}
