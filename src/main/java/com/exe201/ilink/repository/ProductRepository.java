package com.exe201.ilink.repository;

import com.exe201.ilink.model.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {


    @Query("SELECT p FROM Product p WHERE p.shop.shopId = :shopId and p.status = 'ACTIVE'")
    Page<Product> findByShopId(Long shopId, Pageable pageable);
}
