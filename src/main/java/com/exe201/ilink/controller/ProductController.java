package com.exe201.ilink.controller;


import com.exe201.ilink.Util.AppConstants;
import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.exception.ResponseBuilder;
import com.exe201.ilink.model.payload.request.ProductRequest;
import com.exe201.ilink.model.payload.request.UpdateProductRequest;
import com.exe201.ilink.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("product")
@RequiredArgsConstructor
@Tag(name = "Product", description = "Method for product settings required access token to gain access")
public class ProductController {

    private final ProductService productService;

    @Operation(
        summary = "Get all the product in specific shop for seller, manager")
    @GetMapping("/shop")
    public ResponseEntity<Object> getShopProducts(@NotNull @RequestParam("shopId") Long shopId,
                                                  @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                                  @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                                  @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_PRODUCT_SORT_DATE_DSC, required = false) ProductSort sortBy,
                                                  @RequestParam(name = "keyword", required = false) String keyword,
                                                  @RequestParam(name = "minPrice", required = false) Double minPrice,
                                                  @RequestParam(name = "maxPrice", required = false) Double maxPrice
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved products",
            productService.getShopProducts(shopId, pageNo, pageSize, sortBy, minPrice, maxPrice, keyword));
    }

    @Operation(
        summary = "Get all the product to list in homepage for seller, manager")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllProducts(@RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                                 @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                                 @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_PRODUCT_SORT_DATE_DSC, required = false) ProductSort sortBy,
                                                 @RequestParam(name = "keyword", required = false) String keyword,
                                                 @RequestParam(name = "minPrice", required = false) Double minPrice,
                                                 @RequestParam(name = "maxPrice", required = false) Double maxPrice

    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved products",
            productService.getAllOrSearchProducts(pageNo, pageSize, sortBy, keyword, minPrice, maxPrice));
    }

    @Operation(
        summary = "Get details of product for user")
    @GetMapping("/details")
    public ResponseEntity<Object> getProductDetails(@RequestParam(name = "productId") Long productId
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved products", productService.getProductDetails(productId));
    }


    @PostMapping(value = "/new")
    public ResponseEntity<Object> addProduct(@NotNull @RequestBody ProductRequest productRequest
    ) {
        productService.addProduct(productRequest);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Request accepted. add product successfully, please wait for manager approval");
    }

    @PostMapping(value = "/picture", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Object> addPicture(@NotNull @RequestParam("productId") Long productId,
                                             @RequestParam("picture_file") MultipartFile file
    ) throws IOException {
        productService.addPicture(productId, file);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Picture updated successfully");
    }

    @PutMapping(value = "/edit")
    public ResponseEntity<Object> editProduct(@NotNull @RequestParam("productId") Long productId,
                                              @RequestBody UpdateProductRequest productRequest
    ) {
        productService.updateProduct(productId, productRequest);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Request accepted. edit product successfully");
    }

    @PutMapping(value = "/approve")
    public ResponseEntity<Object> manageProduct(@NotNull @RequestParam("productId") Long productId,
                                                @NotNull @RequestParam("status") String status
    ) {
        productService.manageProduct(productId, status);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Request accepted. operation execute successfully");
    }

}
