package com.exe201.ilink.service;

import com.exe201.ilink.model.payload.dto.ShopDTO;

public interface ShopService {

    ShopDTO getShopById(Long shopId);
}
