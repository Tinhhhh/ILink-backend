package com.exe201.ilink.service;

import com.exe201.ilink.model.entity.ProductCategory;
import com.exe201.ilink.model.payload.dto.ProductCategoryDTO;

import java.util.List;

public interface ProductCategoryService {

    void addCategory(String name);

    List<ProductCategoryDTO> getAllCategory();
}
