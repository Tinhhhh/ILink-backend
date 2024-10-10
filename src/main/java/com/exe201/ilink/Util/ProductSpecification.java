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
        return (root, query, cb) -> prodName == null ? null : cb.like(root.get("productName"), "%" + prodName + "%");
    }

    public Specification<Product> hasCateName(String cateName) {
        return (root, query, cb) -> {
            if (cateName == null) return null;

            Join<Product, ProductCategory> join = root.join("category");
            return cb.like(join.get("name"), "%" + cateName + "%");
        };
    }

    public Specification<Product> hasShopName(String shopName) {
        return (root, query, cb) -> {
            if (shopName == null) return null;

            Join<Product, Shop> join = root.join("shop");
            return cb.like(join.get("shopName"), "%" + shopName + "%");
        };
    }

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
