package com.exe201.ilink.controller;

import com.exe201.ilink.model.exception.ResponseBuilder;
import com.exe201.ilink.service.ShopService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("shop")
@Tag(name = "Shop", description = "Method for shop settings required access token to gain access")
public class ShopController {

    private final ShopService shopService;

    @GetMapping("/get")
    public ResponseEntity<Object> getShop(@NotNull @RequestParam("shopId") Long shopId) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved Shop information", shopService.getShopById(shopId));
    }

}
