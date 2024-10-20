package com.exe201.ilink.service.Impl;

import com.exe201.ilink.config.converter.GenericConverter;
import com.exe201.ilink.model.entity.Product;
import com.exe201.ilink.model.entity.ProductCategory;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.payload.dto.ProductCategoryDTO;
import com.exe201.ilink.repository.ProductCategoryRepository;
import com.exe201.ilink.service.CloudinaryService;
import com.exe201.ilink.service.ProductCategoryService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductCategoryServiceImplement implements ProductCategoryService {

    private final ProductCategoryRepository productCategoryRepository;
    private final GenericConverter<ProductCategory> genericConverter;
    private final ModelMapper modelMapper;
    private final CloudinaryService cloudinaryService;

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

    @Override
    @Transactional
    public void addPicture(Long categoryId, MultipartFile file) throws IOException {
        ProductCategory category = productCategoryRepository.findById(categoryId)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Categoy picture update fails. Category not found, please contact the administrator."));

        String imgUrl = cloudinaryService.uploadFile(file);

        category.setImage(imgUrl);
        productCategoryRepository.save(category);
    }


}
