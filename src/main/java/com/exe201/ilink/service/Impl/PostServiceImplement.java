package com.exe201.ilink.service.Impl;

import com.exe201.ilink.Util.PostSpecification;
import com.exe201.ilink.config.converter.GenericConverter;
import com.exe201.ilink.model.entity.Post;
import com.exe201.ilink.model.entity.PostDetail;
import com.exe201.ilink.model.entity.Product;
import com.exe201.ilink.model.entity.Shop;
import com.exe201.ilink.model.enums.PostStatus;
import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.payload.request.NewPostRequest;
import com.exe201.ilink.model.payload.request.UpdatePostRequest;
import com.exe201.ilink.model.payload.response.ListPostResponse;
import com.exe201.ilink.model.payload.response.PostResponse;
import com.exe201.ilink.model.payload.response.ProductResponse;
import com.exe201.ilink.repository.PostDetailRepository;
import com.exe201.ilink.repository.PostRepository;
import com.exe201.ilink.repository.ProductRepository;
import com.exe201.ilink.repository.ShopRepository;
import com.exe201.ilink.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class PostServiceImplement implements PostService {

    private final PostRepository postRepository;
    private final ShopRepository shopRepository;
    private final ProductRepository productRepository;
    private final GenericConverter<Post> genericConverter;
    private final PostDetailRepository postDetailRepository;

    @Override
    @Transactional
    public void createPost(NewPostRequest postRequest) {
        //Check if shop exist
        Shop shop = shopRepository.findById(postRequest.getShopId())
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Post creation fails. Shop not found, please contact the administrator."));

        //convert request to entity
        Post newPost = genericConverter.toEntity(postRequest, Post.class);
        newPost.setShop(shop);
        newPost.setStatus(PostStatus.ACTIVE.getStatus());
        //Save post
        postRepository.save(newPost);

        //Create post detail
        List<PostDetail> postDetails = new ArrayList<>();

        if (postRequest.getProducts().isEmpty()) {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "Post creation fails. Product list is empty.");
        }

        //Check if product exist
        postRequest.getProducts().forEach(productId -> {
            PostDetail postDetail = new PostDetail();
            postDetail.setPost(newPost);
            //Check if product is valid, Status = ACTIVE
            postDetail.setProduct(productRepository.findByProductId(productId)
                .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Post creation fails. Product not found or still pending, please contact the administrator.")));

            //Check if product is belong to shop
            if (!postDetail.getProduct().getShop().getShopId().equals(shop.getShopId())) {
                throw new ILinkException(HttpStatus.BAD_REQUEST, "Post creation fails. Product not belong to shop, please contact the administrator.");
            }

            postDetails.add(postDetail);
        });
        postDetailRepository.saveAll(postDetails);

    }

    @Override
    public PostResponse getPostsDetails(Long postId) {

        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Post not found, please contact the administrator."));

        List<PostDetail> postDetail = postDetailRepository.findByPostId(post.getId())
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Post detail not found, please contact the administrator."));

        List<Product> products = new ArrayList<>();

        postDetail.stream().forEach(postItem -> {
            products.add(postItem.getProduct());
        });
        List<ProductResponse> productResponses = new ArrayList<>();

        products.stream().forEach(product -> {
            productResponses.add(ProductResponse.builder()
                .status(product.getStatus())
                .productId(product.getId())
                .productName(product.getProductName())
                .description(product.getDescription())
                .price(product.getPrice())
                .image(product.getImage())
                .stock(product.getStock())
                .categoryName(product.getCategory().getName())
                .shopId(product.getShop().getShopId())
                .shopName(product.getShop().getShopName())
                .createdDate(product.getCreatedDate().toString())
                .build());
        });


        return PostResponse.builder()
            .id(post.getId())
            .title(post.getTitle())
            .description(post.getDescription())
            .status(post.getStatus())
            .products(productResponses)
            .createdDate(post.getCreatedDate().toString())
            .build();

    }

    @Override
    public ListPostResponse getShopPost(Long shopId, int pageNo, int pageSize, ProductSort sortBy, String keyword) {

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Shop not found, please contact the administrator."));

        Specification<Post> spec = Specification.where(PostSpecification.hasShopId(shopId).and(PostSpecification.notHidden())) //Điều kiện tiên quyết
            .and(
                Specification.where(PostSpecification.hasPostTitle(keyword))
                    .or(PostSpecification.hasProdName(keyword))
                    .or(PostSpecification.hasCateName(keyword))
            );

        return getListPostResponse(pageNo, pageSize, sortBy, spec);
    }

    @Override
    public ListPostResponse getAllOrSearchPosts(int pageNo, int pageSize, ProductSort sortBy, String keyword, Double minPrice, Double maxPrice) {

        if (keyword == null && minPrice == null && maxPrice == null) {
            return getListPostResponse(pageNo, pageSize, sortBy, PostSpecification.notHidden());
        }

        Specification<Post> spec = Specification.where(PostSpecification.notHidden()) //Điều kiện tiên quyết
            .and(
                Specification.where(PostSpecification.hasPostTitle(keyword))
                    .or(PostSpecification.hasShopName(keyword))
                    .or(PostSpecification.hasProdName(keyword))
                    .or(PostSpecification.hasCateName(keyword))
                    .or(PostSpecification.hasPrice(minPrice, maxPrice))
            );


        return getListPostResponse(pageNo, pageSize, sortBy, spec);
    }

    @Override
    @Transactional
    public void updatePost(Long postId, UpdatePostRequest postRequest) {

        //Check if post exist
        Post post = postRepository.findById(postId)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Post not found, please contact the administrator."));

        //Check if status is valid
        if (!PostStatus.contains(postRequest.getStatus())) {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "Post update fails. Invalid status.");
        }

        //Check if product list is empty
        if (postRequest.getProducts().isEmpty()) {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "Post update fails. Product list is empty.");
        }

        //Update post
        Post newPost = genericConverter.updateEntity(postRequest, post);
        postRepository.save(newPost);

        //find old post detail to renew product lis
        List<PostDetail> postDetails = postDetailRepository.findByPostId(postId)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Post detail not found, please contact the administrator."));

        List<Long> productIds = new ArrayList<>();
        postDetails.forEach(
            postDetail -> productIds.add(postDetail.getProduct().getId())
        );

        //Delete old post detail and create new post detail
        Set<Long> newProductIds = new HashSet<>(postRequest.getProducts());
        Set<Long> oldProductIds = new HashSet<>(productIds);

        //Create Post Detail List
        Set<Long> addProductList = new HashSet<>(newProductIds);
        addProductList.removeAll(oldProductIds);

        //Delete Post Detail
        Set<Long> deleteProductList = new HashSet<>(oldProductIds);
        deleteProductList.removeAll(newProductIds);

        for (Long l : deleteProductList) {

            PostDetail postDetail = postDetailRepository.findByProductId(l)
                .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Post detail not found, please contact the administrator."));

            postDetailRepository.deleteById(postDetail.getId());
        }

        postDetails.clear();

        addProductList.stream().forEach(productId -> {
            PostDetail postDetail = new PostDetail();
            postDetail.setPost(newPost);

            //Check if product is valid, Status = ACTIVE
            postDetail.setProduct(productRepository.findByProductId(productId)
                .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Post creation fails. Product not found or still pending, please contact the administrator.")));

            //Check if product is belong to shop
            if (!postDetail.getProduct().getShop().getShopId().equals(post.getShop().getShopId())) {
                throw new ILinkException(HttpStatus.BAD_REQUEST, "Post creation fails. Product not belong to shop, please contact the administrator.");
            }
            postDetails.add(postDetail);
            postDetailRepository.save(postDetail);
        });

    }

    private ListPostResponse getListPostResponse(int pageNo, int pageSize, ProductSort sortBy, Specification<Post> spec) {
        Sort sort = Sort.by(sortBy.getDirection(), "createdDate");
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Post> posts = postRepository.findAll(spec, pageable);
        if (posts.isEmpty()) {
            return ListPostResponse.builder()
                .content(null)
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(0)
                .totalPages(0)
                .last(true)
                .build();
        }

        List<Post> products = posts.getContent();
        List<PostResponse> postResponses = new ArrayList<>();
        products.stream().forEach(post -> {
            postResponses.add(this.getPostsDetails(post.getId()));
        });

        return ListPostResponse.builder()
            .content(postResponses)
            .pageNo(posts.getNumber())
            .pageSize(posts.getSize())
            .totalElements(posts.getTotalElements())
            .totalPages(posts.getTotalPages())
            .last(posts.isLast())
            .build();
    }

}
