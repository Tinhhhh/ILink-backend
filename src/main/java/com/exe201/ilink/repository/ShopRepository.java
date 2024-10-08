package com.exe201.ilink.repository;

import com.exe201.ilink.model.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    @Query("SELECT s FROM Shop s WHERE s.account.accountId =:accountId")
    Optional<Shop> findByAccountId(UUID accountId);
}
