package com.exe201.ilink.service;


import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.payload.dto.request.ProductRequest;
import com.exe201.ilink.model.payload.dto.request.UpdateProductRequest;
import com.exe201.ilink.model.payload.dto.response.ProductResponse;
import com.exe201.ilink.model.payload.dto.response.ShopProductResponse;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ProductService {

    void addProduct(ProductRequest product);

    void updateProduct(Long id, UpdateProductRequest product);

    ShopProductResponse getShopProducts(Long shopId, int pageNo, int pageSize, ProductSort sortBy, Double minPrice, Double maxPrice, String keyword);

    ShopProductResponse getAllOrSearchProducts(int pageNo, int pageSize, ProductSort sortBy, String keyword, Double minPrice, Double maxPrice);

    ProductResponse getProductDetails(Long productId);

    void addPicture(Long productId, MultipartFile file) throws IOException;
}
