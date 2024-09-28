package com.exe201.ilink.controller;

import com.exe201.ilink.model.exception.CustomSuccessHandler;
import com.exe201.ilink.model.payload.dto.request.ChangePasswordRequest;
import com.exe201.ilink.service.AccountService;
import com.exe201.ilink.service.AuthenService;
import com.exe201.ilink.service.CloudinaryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Method for account settings required access token to gain access")
public class AccountController {

    private final AuthenService authService;
    private final CloudinaryService cloudinaryService;
    private final AccountService accountService;

    @Operation(
        summary = "View user profile",
        description = "View current user information after logging into the system. Passwords, tokens, etc., " +
            "will not be displayed",
        tags = {"Account"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Account information retrieve successfully",
            content = @Content(
                examples = @ExampleObject(value = """
                        {
                           "http_status": 200,
                           "time_stamp": "06/02/2024 17:25:41",
                           "message": "Successfully retrieved user information",
                           "data": {
                             "userId": "a9126139-3c11-47ba-8493-cf7e480c3645",
                             "first_name": "James",
                             "last_name": "Bond",
                             "email": "Sniper6969@gmail.com",
                             "address": "123 Main St, Springfield",
                             "gender": null,
                             "phone": "0877643231",
                             "profile_pic": null,
                             "auth_provider": null,
                             "role_name": "USER"
                           }
                         }
                    """))),
        @ApiResponse(responseCode = "401", description = "No JWT token found in the request header"),
    })
    @GetMapping("/profile")
    public ResponseEntity<Object> getCurrentAccountInfo(HttpServletRequest request) {
        return CustomSuccessHandler.responseBuilder(HttpStatus.OK,
            "Successfully retrieved user information", accountService.getCurrentAccountInfo(request));
    }

    @PostMapping("/change-password")
    @ResponseStatus(HttpStatus.OK)
    public String changePassword(@RequestBody ChangePasswordRequest changePasswordRequest, HttpServletRequest request) {
        accountService.changePassword(changePasswordRequest, request);
        return "Password changed successfully";
    }

    @Operation(
        summary = "Update user profile picture",
        description = "Update current user profile picture after logging into the system.",
        tags = {"Account"})
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User profile picture updated successfully"),
        @ApiResponse(responseCode = "500", description = "Failed to update user profile picture"),
    })
    @PostMapping(value = "/image", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @ResponseStatus(HttpStatus.OK)
    public String updateUserProfilePicture(@NotNull UUID id,
                                           @RequestParam(value = "profile_pic", required = false) MultipartFile profilePicture) throws IOException {

        String imageURLMain = cloudinaryService.uploadFile(profilePicture);
        accountService.updateAccountProfilePicture(id, imageURLMain);
        return "User profile picture updated successfully";
    }


    @Operation(summary = "Logout of the system", description = "Logout of the system, bearer token (refresh token) is required")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Logged out successfully"), @ApiResponse(responseCode = "401", description = "No JWT token found in the request header")})
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        authService.logout(request, response, authentication);
        return ResponseEntity.ok().body("Logged out successfully");
    }


}
