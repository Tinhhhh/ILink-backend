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
import com.exe201.ilink.model.payload.dto.request.NewPostRequest;
import com.exe201.ilink.model.payload.dto.response.ListPostResponse;
import com.exe201.ilink.model.payload.dto.response.PostResponse;
import com.exe201.ilink.model.payload.dto.response.ProductResponse;
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
import java.util.List;

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
        Shop shop = shopRepository.findById(postRequest.getShopId())
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Post creation fails. Shop not found, please contact the administrator."));

        Post newPost = genericConverter.toEntity(postRequest, Post.class);
        newPost.setShop(shop);
        newPost.setStatus(PostStatus.ACTIVE.getStatus());
        postRepository.save(newPost);

        List<PostDetail> postDetails = new ArrayList<>();

        if (postRequest.getProductIdList().isEmpty()) {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "Post creation fails. Product list is empty.");
        }

        postRequest.getProductIdList().forEach(productId -> {
            PostDetail postDetail = new PostDetail();
            postDetail.setPost(newPost);
            //Status = ACTIVE
            postDetail.setProduct(productRepository.findByProductId(productId)
                .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Post creation fails. Product not found or still pending, please contact the administrator.")));
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
                .shopName(product.getProductName())
                .build());
        });


        return PostResponse.builder()
            .id(post.getId())
            .title(post.getTitle())
            .description(post.getDescription())
            .status(post.getStatus())
            .products(productResponses)
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

    private ListPostResponse getListPostResponse(int pageNo, int pageSize, ProductSort sortBy, Specification<Post> spec) {
        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
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
