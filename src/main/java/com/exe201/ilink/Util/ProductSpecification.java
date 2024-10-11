package com.exe201.ilink.Util;

import com.exe201.ilink.model.entity.Product;
import com.exe201.ilink.model.entity.ProductCategory;
import com.exe201.ilink.model.entity.Shop;
import jakarta.persistence.criteria.Join;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;

@UtilityClass
public class ProductSpecification {

    public Specification<Product> hasShopId(Long shopId) {
        return (root, query, cb) -> {
            if (shopId == null) return null;
            Join<Product, Shop> join = root.join("shop");
            return cb.equal(join.get("shopId"), shopId);
        };
    }

    public Specification<Product> hasProdName(String prodName) {
        return (root, query, cb) -> {
            if (prodName == null) return null;

            return cb.like(cb.lower(root.get("productName")),
                "%" + prodName.toLowerCase() + "%"
            );
        };
    }

    public Specification<Product> hasProdNameWithRemoveDiacritics(String prodName) {
        return (root, query, cb) -> {
            if (prodName == null) return null;
            String normalizedProdName = StringUtils.removeDiacritics(prodName.toLowerCase());

            return cb.like(cb.lower(cb.function("unaccent", String.class, root.get("productName"))),
                "%" + normalizedProdName + "%"
            );
        };
    }

    public Specification<Product> hasCateName(String cateName) {
        return (root, query, cb) -> {
            if (cateName == null) return null;

            Join<Product, ProductCategory> join = root.join("category");
            return cb.like(cb.lower(join.get("name")), "%" + cateName.toLowerCase() + "%");
        };
    }

    public Specification<Product> hasShopName(String shopName) {
        return (root, query, cb) -> {
            if (shopName == null) return null;

            Join<Product, Shop> join = root.join("shop");
            return cb.like(cb.lower(join.get("shopName")), "%" + shopName.toLowerCase() + "%");
        };
    }


    public Specification<Product> hasStatus(String status) {
        return (root, query, cb) -> status == null ? null : cb.equal(cb.lower(root.get("status")), status);
    }

//    public Specification<Product> hasPostTitle(String postTitle) {
//        return (root, query, cb) -> {
//            if (postTitle == null) return null;
//
//            Join<Product, Shop> join = root.join("shop");
//            return cb.like(join.get("shopName"), "%" + shopName + "%");
//        };
//    }

    //Filter by price
    public Specification<Product> hasPrice(Double minPrice, Double maxPrice) {
        return (root, query, cb) -> {
            if (minPrice == null && maxPrice == null) return null;
            if (minPrice != null && maxPrice != null)
                return cb.between(root.get("price"), minPrice, maxPrice);
            if (minPrice != null)
                return cb.greaterThanOrEqualTo(root.get("price"), minPrice);
            else return cb.lessThanOrEqualTo(root.get("price"), maxPrice);

        };
    }


}
