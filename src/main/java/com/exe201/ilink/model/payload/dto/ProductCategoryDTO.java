package com.exe201.ilink.model.payload.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryDTO {
    @JsonProperty("category_id")
    private Long id;

    private String name;

    private String image;

}
