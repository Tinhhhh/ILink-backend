package com.exe201.ilink.controller;


import com.exe201.ilink.Util.AppConstants;
import com.exe201.ilink.model.exception.CustomSuccessHandler;
import com.exe201.ilink.model.payload.dto.request.ProductRequest;
import com.exe201.ilink.service.ProductService;
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

    @GetMapping("/products")
    public ResponseEntity<Object> getShopProducts(@NotNull @RequestParam("accountId") UUID accountId,
                                                  @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                                  @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_SORT_BY, required = false) String sortBy,
                                                  @RequestParam(name = "sortDir", defaultValue = AppConstants.DEFAULT_SORT_DIRECTION, required = false) String sortDir
    ){
        return CustomSuccessHandler.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved products", productService.getShopProducts(accountId, pageNo, pageSize, sortBy, sortDir));
    }

    @PostMapping("/new-product")
    public Map<String, Object> addProduct(@NotNull @RequestParam("accountId") UUID accountId,
                                          @RequestBody ProductRequest productRequest){
        productService.addProduct(accountId, productRequest);
        return CustomSuccessHandler.responseBuilder(HttpStatus.OK, "Product added successfully");
    }

}
