package com.exe201.ilink.service;

import com.exe201.ilink.model.payload.dto.ProductCategoryDTO;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface ProductCategoryService {

    void addCategory(String name);

    List<ProductCategoryDTO> getAllCategory();

    void addPicture(Long categoryIds, MultipartFile file) throws IOException;
}
