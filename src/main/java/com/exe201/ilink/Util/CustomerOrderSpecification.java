package com.exe201.ilink.Util;

import com.exe201.ilink.model.entity.*;
import com.exe201.ilink.model.enums.PaymentStatus;
import com.exe201.ilink.model.exception.ILinkException;
import jakarta.persistence.criteria.Join;
import lombok.experimental.UtilityClass;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;

import java.util.Date;
import java.util.UUID;

@UtilityClass
public class CustomerOrderSpecification {

    public Specification<CustomerOrder> hasBuyerId(UUID buyerId) {
        return (root, query, cb) -> {
            if (buyerId == null) return null;

            Join<CustomerOrder, Account> join = root.join("account");
            return cb.equal((join.get("accountId")), buyerId);
        };
    }

    public Specification<CustomerOrder> hasSellerId(UUID sellerId) {
        return (root, query, cb) -> {
            if (sellerId == null) return null;

            Join<CustomerOrder, OrderDetail> orderDetailJoin = root.join("orderDetails");
            Join<OrderDetail, Product> orderDetailProductJoin = orderDetailJoin.join("product");
            Join<Product, Shop> productShopJoin = orderDetailProductJoin.join("shop");
            Join<Shop, Account> shopAccountJoin = productShopJoin.join("account");

            return cb.equal((shopAccountJoin.get("accountId")), sellerId);
        };
    }

    public Specification<CustomerOrder> hasStatus(String status) {
        return (root, query, cb) -> {
            if (status == null) return null;
            if (!PaymentStatus.isContains(status)) {
                throw new ILinkException(HttpStatus.BAD_REQUEST, "Request fails, status is not valid");
            }
            return cb.equal((root.get("status")), status);
        };
    }

    public Specification<CustomerOrder> isCreatedBetween(Date startDate, Date endDate) {
        return (root, query, cb) -> {
            if (startDate == null || endDate == null) return null;

            // Sử dụng biểu thức between để kiểm tra ngày nằm trong khoảng từ startDate đến endDate
            return cb.between(root.get("createdDate"), startDate, endDate);
        };
    }


}
