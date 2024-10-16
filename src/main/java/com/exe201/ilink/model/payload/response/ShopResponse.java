package com.exe201.ilink.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShopResponse {

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("shop_name")
    private String shopName;

    private String description;

    private String address;

    @JsonProperty("reputation")
    private int reputation;

    @JsonProperty("is_locked")
    private boolean isLocked;

    @JsonProperty("created_date")
    private Date createdDate;

    @JsonProperty("shop_avatar")
    private String shopAvatar;

}
