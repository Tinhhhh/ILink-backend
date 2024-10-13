package com.exe201.ilink.model.payload.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdatePostRequest {
    @Column(name = "title")
    private String title;

    @Schema(description = "Product's description", example = "The handmade gift for your loved ones.")
    private String description;

    @Schema(description = "Post's status", example = "CLOSED")
    private String status;

    @Schema(description = "Product's id", example = "1")
    private List<Long> products;
}
