package com.exe201.ilink.model.payload.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Request object for changing password")
public class ChangePasswordRequest {

    @Schema(description = "User's email address", example = "john.doe@example.com")
    @NotBlank(message = "Email cannot be blank")
    @Email(message = "Invalid email format")
    private String email;

    @Schema(description = "User's old password", example = "Password1")
    @NotBlank(message = "Password cannot be blank")
    private String oldPassword;

    @Schema(description = "User's new password", example = "Password1")
    @NotBlank(message = "Password cannot be blank")
    private String newPassword;

}
