package com.exe201.ilink.service.Impl;

import com.exe201.ilink.config.converter.GenericConverter;
import com.exe201.ilink.model.entity.ProductCategory;
import com.exe201.ilink.model.payload.dto.ProductCategoryDTO;
import com.exe201.ilink.repository.ProductCategoryRepository;
import com.exe201.ilink.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImplement implements ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final GenericConverter<ProductCategory> genericConverter;
    private final ModelMapper modelMapper;

    @Override
    public void addCategory(String name) {
        ProductCategory productCategory = new ProductCategory();
        productCategory.setName(name);
        productCategoryRepository.save(productCategory);
    }

    @Override
    public List<ProductCategoryDTO> getAllCategory() {
        List<ProductCategory> productCategories = productCategoryRepository.findAll();
        List<ProductCategoryDTO> productCategoryDTOList = new ArrayList<>();
        productCategories.stream().forEach(productCategory -> {
            ProductCategoryDTO productCategoryDTO = modelMapper.map(productCategory, ProductCategoryDTO.class);
            productCategoryDTOList.add(productCategoryDTO);
        });

        return productCategoryDTOList;
    }


}
