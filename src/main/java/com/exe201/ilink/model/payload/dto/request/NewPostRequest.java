package com.exe201.ilink.model.payload.dto.request;

import com.exe201.ilink.model.entity.Product;
import com.exe201.ilink.model.entity.Shop;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.util.Date;
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
    private long shopId;

    @Schema(description = "Product's id", example = "1")
    private List<Long> productIdList;

}
