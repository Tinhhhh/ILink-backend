package com.exe201.ilink.service;

import com.exe201.ilink.model.payload.response.ShopResponse;

public interface ShopService {

    ShopResponse getShopById(Long shopId);
}
