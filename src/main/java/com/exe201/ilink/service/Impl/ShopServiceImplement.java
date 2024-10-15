package com.exe201.ilink.service.Impl;

import com.exe201.ilink.model.entity.Shop;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.payload.dto.ShopDTO;
import com.exe201.ilink.repository.ShopRepository;
import com.exe201.ilink.service.ShopService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ShopServiceImplement implements ShopService {
    private final ShopRepository shopRepository;
    @Override
    public ShopDTO getShopById(Long shopId) {
        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Request fail. "));





        return null;
    }
}
