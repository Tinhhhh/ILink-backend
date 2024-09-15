package com.exe201.ilink.controller;

import com.exe201.ilink.exception.CustomSuccessHandler;
import com.exe201.ilink.exception.ExceptionResponse;
import com.exe201.ilink.model.payload.dto.request.AuthenticationRequest;
import com.exe201.ilink.model.payload.dto.request.RegistrationRequest;
import com.exe201.ilink.service.AuthenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("auth")
//@CrossOrigin(origins = "") cau hinh CORS
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication required to use other resources.")
public class AuthController {

    private final AuthenService authService;

    @Operation(summary = "Register a new account", description = "Perform to register a new account, all the information must be filled out and cannot be blank, once requested a verification email will be send")
    @ApiResponses(value = {@ApiResponse(responseCode = "202", description = "Successfully Registered", content = @Content(examples = @ExampleObject(value = """
            
            """))), @ApiResponse(responseCode = "400", description = "Validation error", content = @Content(schema = @Schema(implementation = ExceptionResponse.class), examples = @ExampleObject(value = """
            """)))})
    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ResponseEntity<Object> Register(@RequestBody @Valid RegistrationRequest request) {
        authService.register(request);
        return CustomSuccessHandler.responseBuilder(HttpStatus.ACCEPTED, "Successfully Register", "Please check your email for account verification.");
    }

    @Operation(summary = "Login in to the system", description = "Login into the system requires all information to be provided, " + "and validations will be performed. The response will include an access token and a refresh token")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully SignIn", content = @Content(examples = @ExampleObject(value = """
                {
                   "http_status": 200,
                   "time_stamp": "10/29/2024 11:20:03",
                   "message": "Successfully SignIn",
                   "data": {
                     "access_token": "xxxx.yyyy.zzzz",
                     "refresh_token": "xxxx.yyyy.zzzz"
                }
            """))), @ApiResponse(responseCode = "401", description = "Unauthorized", content = @Content(examples = @ExampleObject(value = """
                {
                 "http_status": 401,
                 "time_stamp": "05/29/2024 21:24:57",
                 "message": "Email or Password is incorrect"
               }
            """))), @ApiResponse(responseCode = "401", description = "Account Locked", content = @Content(examples = @ExampleObject(value = """
                {
                 "http_status": 401,
                 "time_stamp": "05/29/2024 21:24:57",
                 "message": "Account is locked please contact administrator for more information"
               }
            """))), @ApiResponse(responseCode = "401", description = "Account Disabled", content = @Content(examples = @ExampleObject(value = """
                {
                 "http_status": 401,
                 "time_stamp": "05/26/2024 21:24:57",
                 "message": "Account is disabled please contact administrator for more information"
               }
            """))),})
    @PostMapping("/signin")
    @ResponseStatus(HttpStatus.OK)
    public ResponseEntity<Object> SignIn (@RequestBody @Valid AuthenticationRequest request){
        return CustomSuccessHandler.responseBuilder(HttpStatus.OK, "Successfully Sign in", authService.authenticate(request));
    }

    @Operation(summary = "Activate account", description = "Activate account after registration successfully, the user will need to enter the 6-digit confirmation code sent to their email to activate the account.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Account verification successfully"),
            @ApiResponse(responseCode = "400", description = "Account Verification Failed"),
            @ApiResponse(responseCode = "400", description = "Your account is already activated"),
            @ApiResponse(responseCode = "400", description = "This activation code is invalid as it has been revoked. Please use the latest activation code sent to your email."),
            @ApiResponse(responseCode = "400", description = "Activation code has expired. A new code has been sent to your email address"),})
    @GetMapping("/activation")
    @ResponseStatus(HttpStatus.OK)
    public void accountActivation(@RequestParam String code, HttpServletResponse response) {
        authService.activeAccount(code, response);
    }


//    @Operation(summary = "Logout of the system", description = "Logout of the system, bearer token (refresh token) is required")
//    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Logged out successfully"), @ApiResponse(responseCode = "401", description = "No JWT token found in the request header")})
//    @PostMapping("/logout")
//    @ResponseStatus(HttpStatus.OK)
//    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
//        logoutServiceConfig.logout(request, response, authentication);
//    }
//
//    @Operation(summary = "Refresh token if expired", description = "If the current JWT Refresh Token has expired or been revoked, you can refresh it using this method")
//    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Generate new Refresh Token and Access Token successfully", content = @Content(examples = @ExampleObject(value = """
//                {
//                 "access_token": "xxxx.yyyy.zzzz",
//                 "refresh_token": "xxxx.yyyy.zzzz"
//               }
//            """))), @ApiResponse(responseCode = "401", description = "No JWT token found in the request header"), @ApiResponse(responseCode = "401", description = "JWT token has expired and revoked")})
//    @PostMapping("/refresh-token")
//    @ResponseStatus(HttpStatus.OK)
//    public void refreshToken(HttpServletRequest request, HttpServletResponse response) throws IOException, JOSEException {
//        authService.refreshToken(request, response);
//    }
//
//    @Operation(summary = "Social Login in to the system using Google (Mobile)", description = "Login into the system using Google. The response will include an access token and a refresh token")
//    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Successfully SignIn with Google", content = @Content(examples = @ExampleObject(value = """
//                {
//                   "http_status": 200,
//                   "time_stamp": "10/29/2024 11:20:03",
//                   "message": "Successfully SignIn with Google",
//                   "data": {
//                     "access_token": "xxxx.yyyy.zzzz",
//                     "refresh_token": "xxxx.yyyy.zzzz"
//                }
//            """))), @ApiResponse(responseCode = "401", description = "Account Locked", content = @Content(examples = @ExampleObject(value = """
//                {
//                 "http_status": 401,
//                 "time_stamp": "05/29/2024 21:24:57",
//                 "message": "Account is locked please contact administrator for more information"
//               }
//            """))), @ApiResponse(responseCode = "401", description = "Account Disabled", content = @Content(examples = @ExampleObject(value = """
//                {
//                 "http_status": 401,
//                 "time_stamp": "05/26/2024 21:24:57",
//                 "message": "Account is disabled please contact administrator for more information"
//               }
//            """))),})
//    @PostMapping("/google-signin")
//    @CrossOrigin(origins = "*")
//    @ResponseStatus(HttpStatus.OK)
//    public ResponseEntity<?> googleSignIn(@RequestBody GoogleAuthenticationRequest request) throws JOSEException {
//        return CustomSuccessHandler.responseBuilder(HttpStatus.OK, "Successfully SignIn with Google", authService.findOrCreateUser(request));
//    }

}
