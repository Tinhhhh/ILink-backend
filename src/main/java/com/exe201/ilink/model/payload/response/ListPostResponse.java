package com.exe201.ilink.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListPostResponse {
    @JsonProperty(value = "content", index = 1)
    private List<PostResponse> content;

    @JsonProperty(value = "page_no", index = 2)
    private int pageNo;
    @JsonProperty(value = "page_size", index = 3)
    private int pageSize;
    @JsonProperty(value = "total_elements", index = 4)
    private long totalElements;
    @JsonProperty(value = "total_pages", index = 5)
    private int totalPages;
    @JsonProperty(value = "last", index = 6)
    private boolean last;
}
