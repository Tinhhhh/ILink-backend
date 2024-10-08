package com.exe201.ilink.service.Impl;

import com.exe201.ilink.model.entity.Product;
import com.exe201.ilink.model.entity.ProductCategory;
import com.exe201.ilink.model.entity.Shop;
import com.exe201.ilink.model.enums.ProductStatus;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.payload.dto.request.ProductRequest;
import com.exe201.ilink.model.payload.dto.response.ProductResponse;
import com.exe201.ilink.model.payload.dto.response.ShopProductResponse;
import com.exe201.ilink.repository.PostRepository;
import com.exe201.ilink.repository.ProductCategoryRepository;
import com.exe201.ilink.repository.ProductRepository;
import com.exe201.ilink.repository.ShopRepository;
import com.exe201.ilink.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductServiceImplement implements ProductService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ModelMapper modelMapper;

    @Override
    public void addProduct(UUID accountId, ProductRequest product) {
        Shop shop = shopRepository.findByAccountId(accountId)
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Product creation fails. Shop not found, please contact the administrator."));

        ProductCategory productCategory = productCategoryRepository.findById(product.getCategory())
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Product creation fails. Product category not found, please contact the administrator."));


        Product newProduct = modelMapper.map(product, Product.class);
        newProduct.setShop(shop);
        newProduct.setCategory(productCategory);
        newProduct.setStatus(ProductStatus.PENDING.name());
        productRepository.save(newProduct);

    }

    @Override
    public void updateProduct(ProductRequest product) {

    }

    @Override
    public void deleteProduct(ProductRequest product) {

    }

    @Override
    public ShopProductResponse getShopProducts(UUID accountId, int pageNo, int pageSize, String sortBy, String sortDir) {

        Shop shop = shopRepository.findByAccountId(accountId)
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Products retrieves fails. Shop not found, please contact the administrator."));

        Sort sort = sortDir.equals(Sort.Direction.ASC.name()) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Page<Product> productContent = productRepository.findByShopId(shop.getShopId(), pageable);
        List<Product> products = productContent.getContent();
        List<ProductResponse> content = products.stream().map(ProductDTO -> modelMapper.map(ProductDTO, ProductResponse.class)).toList();

        return ShopProductResponse.builder()
            .content(content)
            .pageNo(productContent.getNumber())
            .pageSize(productContent.getSize())
            .totalElements(productContent.getTotalElements())
            .totalPages(productContent.getTotalPages())
            .last(productContent.isLast())
            .build();

    }

}
