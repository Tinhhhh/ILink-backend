package com.exe201.ilink.service.Impl;

import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.model.entity.Shop;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.payload.response.ShopResponse;
import com.exe201.ilink.repository.AccountRepository;
import com.exe201.ilink.repository.ShopRepository;
import com.exe201.ilink.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopServiceImplement implements ShopService {
    private final ShopRepository shopRepository;
    private final AccountRepository accountRepository;

    @Override
    public ShopResponse getShopById(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Request fail. shop not found"));

        Account account = accountRepository.findById(shop.getAccount().getAccountId())
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Request fail. Account not found"));

        return ShopResponse.builder()
            .shopId(shop.getShopId())
            .shopName(shop.getShopName())
            .description(shop.getDescription())
            .address(shop.getAddress())
            .reputation(shop.getReputation())
            .isLocked(shop.isLocked())
            .createdDate(shop.getCreatedDate())
            .shopAvatar(account.getAvatar())
            .build();

    }
}
