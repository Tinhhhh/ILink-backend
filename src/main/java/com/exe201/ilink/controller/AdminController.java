package com.exe201.ilink.controller;

import com.exe201.ilink.Util.AppConstants;
import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.exception.ResponseBuilder;
import com.exe201.ilink.model.payload.response.UpdateAccountResponse;
import com.exe201.ilink.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final AccountService accountService;

    //Lấy danh sách account
    @Operation(
        summary = "Get all the account in the system"
    )
    @GetMapping("/account-list")
    public ResponseEntity<Object> getAccountList(
        @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
        @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
        @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_PRODUCT_SORT_DATE_DSC, required = false) ProductSort sortBy,
        @RequestParam(name = "keyword", required = false) String keyword,
        @RequestParam(name = "role", required = false) String role
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved account",
            accountService.getAllAccount(pageNo, pageSize, sortBy, keyword, role));

    }

    //get account by id
    @Operation(
        summary = "Get detail the account in the system"
    )
    @GetMapping("/account")
    public ResponseEntity<Object> getDetailAccount(
        @RequestParam(name = "accountId", required = true) UUID accountId
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved account",
            accountService.getAccountByAdmin(accountId));

    }

    //Edit account
    @Operation(
        summary = "Edit the account in the system"
    )
    @PutMapping("/account/edit")
    public ResponseEntity<Object> editAccount(
        @RequestParam(name = "accountId", required = true) UUID accountId,
        @RequestBody UpdateAccountResponse updateAccountResponse
    ) {
        accountService.editAccountByAdmin(accountId, updateAccountResponse);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Successfully edited account");


    }


    //Revenue

}
