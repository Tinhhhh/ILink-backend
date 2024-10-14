package com.exe201.ilink.controller;

import com.exe201.ilink.Util.AppConstants;
import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.exception.ResponseBuilder;
import com.exe201.ilink.model.payload.request.NewPostRequest;
import com.exe201.ilink.model.payload.request.UpdatePostRequest;
import com.exe201.ilink.service.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("post")
@RequiredArgsConstructor
@Tag(name = "Post", description = "Method for post settings required access token to gain access")
public class PostController {

    private final PostService postService;

    @Operation(
        summary = "Get all the post in specific shop for user, seller")
    @GetMapping("/shop")
    public ResponseEntity<Object> getShopPosts(@NotNull @RequestParam("shopId") Long shopId,
                                               @RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                               @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                               @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_PRODUCT_SORT_DATE_DSC, required = false) ProductSort sortBy,
                                               @RequestParam(name = "keyword", required = false) String keyword

    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved products",
            postService.getShopPost(shopId, pageNo, pageSize, sortBy, keyword));
    }

    @Operation(
        summary = "Get all the post to list in homepage for user, seller")
    @GetMapping("/all")
    public ResponseEntity<Object> getAllPosts(@RequestParam(name = "pageNo", defaultValue = AppConstants.DEFAULT_PAGE_NUMBER, required = false) int pageNo,
                                              @RequestParam(name = "pageSize", defaultValue = AppConstants.DEFAULT_PAGE_SIZE, required = false) int pageSize,
                                              @RequestParam(name = "sortBy", defaultValue = AppConstants.DEFAULT_PRODUCT_SORT_DATE_DSC, required = false) ProductSort sortBy,
                                              @RequestParam(name = "keyword", required = false) String keyword,
                                              @RequestParam(name = "minPrice", required = false) Double minPrice,
                                              @RequestParam(name = "maxPrice", required = false) Double maxPrice

    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved products",
            postService.getAllOrSearchPosts(pageNo, pageSize, sortBy, keyword, minPrice, maxPrice));
    }

    @Operation(
        summary = "Get details of post for user")
    @GetMapping("/details")
    public ResponseEntity<Object> getPostDetails(@RequestParam(name = "postId") Long postId
    ) {
        return ResponseBuilder.responseBuilderWithData(HttpStatus.OK, "Successfully retrieved pos", postService.getPostsDetails(postId));
    }


    @PostMapping(value = "/new")
    public ResponseEntity<Object> newPost(@NotNull @RequestBody NewPostRequest postRequest
    ) {
        postService.createPost(postRequest);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Request accepted. Post created successfully");
    }

//    @PostMapping(value = "/picture", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
//    public ResponseEntity<Object> addPicture(@NotNull @RequestParam("productId") Long productId,
//                                             @RequestParam("picture_file") MultipartFile file
//    ) throws IOException {
//        postService.addPicture(productId, file);
//        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Picture profile picture updated successfully");
//    }
//

    @PutMapping(value = "/edit")
    public ResponseEntity<Object> editPost(@NotNull @RequestParam("postId") Long postId,
                                           @RequestBody UpdatePostRequest updatePostRequest
    ) {
        postService.updatePost(postId, updatePostRequest);
        return ResponseBuilder.responseBuilder(HttpStatus.OK, "Request accepted. edit post successfully");
    }
}
