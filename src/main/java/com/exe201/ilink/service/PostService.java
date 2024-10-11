package com.exe201.ilink.service;

import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.payload.dto.request.NewPostRequest;
import com.exe201.ilink.model.payload.dto.request.UpdateProductRequest;
import com.exe201.ilink.model.payload.dto.response.ListPostResponse;
import com.exe201.ilink.model.payload.dto.response.PostResponse;
import com.exe201.ilink.model.payload.dto.response.ProductResponse;
import com.exe201.ilink.model.payload.dto.response.ShopProductResponse;

public interface PostService {
    void createPost(NewPostRequest postRequest);

//    void updatePost(Long id, UpdateProductRequest product);

    PostResponse getPostsDetails(Long postId);

    ListPostResponse getShopPost(Long shopId, int pageNo, int pageSize, ProductSort sortBy, String keyword);

    ListPostResponse getAllOrSearchPosts(int pageNo, int pageSize, ProductSort sortBy, String keyword, Double minPrice, Double maxPrice);
}
