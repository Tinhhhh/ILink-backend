package com.exe201.ilink.Util;

import com.exe201.ilink.model.entity.*;
import com.exe201.ilink.model.enums.PostStatus;
import jakarta.persistence.criteria.Join;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class PostSpecification {

    public Specification<Post> hasShopId(Long shopId) {
        return (root, query, cb) -> {
            if (shopId == null) return null;
            Join<Post, Shop> join = root.join("shop");
            return cb.equal(join.get("shopId"), shopId);
        };
    }

    public Specification<Post> hasPostTitle(String title) {
        return (root, query, cb) -> {
            if (title == null) return null;
            return cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
        };
    }


    public Specification<Post> hasProdName(String prodName) {
        return (root, query, cb) -> {
            if (prodName == null) return null;

            // Join từ Post đến PostDetail
            Join<Post, PostDetail> postDetailJoin = root.join("postDetails");

            // Từ PostDetail, Join tiếp đến Product
            Join<PostDetail, Product> productJoin = postDetailJoin.join("product");

            // Điều kiện tìm kiếm theo productName (bỏ qua phân biệt hoa thường)
            return cb.like(cb.lower(productJoin.get("productName")), "%" + prodName.toLowerCase() + "%");
        };
    }

    public Specification<Post> hasCateName(String cateName) {
        return (root, query, cb) -> {
            if (cateName == null) return null;

            // Join từ Post đến PostDetail
            Join<Post, PostDetail> postDetailJoin = root.join("postDetails");

            // Từ PostDetail, Join tiếp đến Product
            Join<PostDetail, Product> productJoin = postDetailJoin.join("product");

            // Từ Product, Join tiếp đến ProductCategory
            Join<Product, ProductCategory> categoryJoin = productJoin.join("category");

            return cb.like(cb.lower(categoryJoin.get("name")), "%" + cateName.toLowerCase() + "%");
        };
    }

    public Specification<Post> hasShopName(String shopName) {
        return (root, query, cb) -> {
            if (shopName == null) return null;

            Join<Post, Shop> join = root.join("shop");
            return cb.like(cb.lower(join.get("shopName")), "%" + shopName.toLowerCase() + "%");
        };
    }

    public Specification<Post> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal((root.get("status")), status);
    }

    public Specification<Post> notHidden() {
        return (root, query, cb) -> cb.notEqual((root.get("status")), PostStatus.HIDDEN.getStatus());
    }


    //Filter by price
    public Specification<Post> hasPrice(Double minPrice, Double maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return null;
            Join<Post, PostDetail> postDetailJoin = root.join("postDetails");
            // Từ PostDetail, Join tiếp đến Product
            Join<PostDetail, Product> productJoin = postDetailJoin.join("product");
            if (minPrice != null && maxPrice != null)
                return cb.between(productJoin.get("price"), minPrice, maxPrice);
            if (minPrice != null)
                return cb.greaterThanOrEqualTo(productJoin.get("price"), minPrice);
            // maxPrice != null && minPrice == null
            return cb.lessThanOrEqualTo(productJoin.get("price"), maxPrice);
        };
    }


}
