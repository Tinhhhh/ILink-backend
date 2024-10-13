package com.exe201.ilink.controller;

import com.exe201.ilink.model.exception.ResponseBuilder;
import com.exe201.ilink.service.ProductCategoryService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("category")
@RestController
@RequiredArgsConstructor
@Tag(name = "Product Category", description = "Method for product category settings required access token to gain access")
public class ProductCategoryController {

    private final ProductCategoryService productCategoryService;

    @PostMapping("/new")
    public ResponseEntity<Object> addCategory(@NotNull @RequestParam String name) {
        productCategoryService.addCategory(name);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Successfully added new category");
    }

    @GetMapping("/all")
    public ResponseEntity<Object> getAllCategory() {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved all category", productCategoryService.getAllCategory());
    }

}
