package com.exe201.ilink.service;


import com.exe201.ilink.model.payload.dto.request.ProductRequest;
import com.exe201.ilink.model.payload.dto.response.ShopProductResponse;

import java.util.UUID;

public interface ProductService {

    void addProduct(UUID accountId, ProductRequest product);

    void updateProduct(ProductRequest product);

    void deleteProduct(ProductRequest product);

    ShopProductResponse getShopProducts(UUID accountId, int pageNo, int pageSize, String sortBy, String sortDir);


}
