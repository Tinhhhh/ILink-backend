package com.exe201.ilink.controller;


import com.exe201.ilink.Util.AppConstants;
import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.exception.ResponseBuilder;
import com.exe201.ilink.model.payload.dto.request.ProductRequest;
import com.exe201.ilink.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("product")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Method for product settings required access token to gain access")
public class ProductController {

    private final ProductService productService;

    @Operation(
        summary = "Get all the product in specific shop for user")
    @GetMapping("/shop")
    public ResponseEntity<Object> getShopProducts(@NotNull @RequestParam("shopId") Long shopId,
                                                  @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                                  @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_PRODUCT_SORT_DATE_DSC, required = false) ProductSort sortBy
    ){
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved products", productService.getShopProducts(shopId, pageNo, pageSize, sortBy));
    }

    @Operation(
        summary = "Get all the product to list in homepage for user")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllProducts(@RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                                  @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_PRODUCT_SORT_DATE_DSC, required = false) ProductSort sortBy
    ){
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved products", productService.getAllProducts(pageNo, pageSize, sortBy));
    }

    @Operation(
        summary = "Get details of product for user")
    @GetMapping("/details")
    public ResponseEntity<Object> getProductDetails(@RequestParam(name = "productId") Long productId
    ){
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved products", productService.getProductDetails(productId));
    }

    @PostMapping("/new")
    public ResponseEntity<Object> addProduct(@NotNull @RequestParam("accountId") UUID accountId,
                                          @RequestBody ProductRequest productRequest){
        productService.addProduct(accountId, productRequest);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Product added successfully");
    }

}
