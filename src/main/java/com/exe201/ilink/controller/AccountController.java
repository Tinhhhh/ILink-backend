package com.exe201.ilink.controller;

import com.exe201.ilink.model.exception.CustomSuccessHandler;
import com.exe201.ilink.model.payload.dto.request.ChangePasswordRequest;
import com.exe201.ilink.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("account")
@RequiredArgsConstructor
@Tag(name = "Account", description = "Method for account settings required access token to gain access")
public class AccountController {

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


}
