package com.exe201.ilink.service;


import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.payload.dto.request.ProductRequest;
import com.exe201.ilink.model.payload.dto.response.ProductResponse;
import com.exe201.ilink.model.payload.dto.response.ShopProductResponse;

import java.util.UUID;

public interface ProductService {

    void addProduct(UUID accountId, ProductRequest product);

    void updateProduct(ProductRequest product);

    void deleteProduct(ProductRequest product);

    ShopProductResponse getShopProducts(Long shopId, int pageNo, int pageSize, ProductSort sortBy);

    ShopProductResponse getAllProducts(int pageNo, int pageSize, ProductSort sortBy);

    ProductResponse getProductDetails(Long productId);
}
