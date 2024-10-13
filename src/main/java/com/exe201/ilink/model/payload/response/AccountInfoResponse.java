package com.exe201.ilink.model.payload.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountInfoResponse {

    @JsonProperty("account_id")
    private UUID accountId;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    private String email;

    private String address;

    private String gender;

    private LocalDate dob;

    private String phone;

    private String avatar;

    @JsonProperty("is_locked")
    private boolean isLocked;

    @JsonProperty("is_enable")
    private boolean isEnable;

    @JsonProperty("role_id")
    private Long roleId;

    @JsonProperty("role_Name")
    private String roleName;

    @JsonProperty("shop_id")
    private Long shopId;

    @JsonProperty("shop_name")
    private String shopName;

}
