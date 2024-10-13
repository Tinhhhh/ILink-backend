package com.exe201.ilink.model.payload.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateAccountResponse {

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

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
}
