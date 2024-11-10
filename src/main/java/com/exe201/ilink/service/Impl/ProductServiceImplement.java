package com.exe201.ilink.service.Impl;

import com.exe201.ilink.Util.ProductSpecification;
import com.exe201.ilink.config.converter.GenericConverter;
import com.exe201.ilink.model.entity.Product;
import com.exe201.ilink.model.entity.ProductCategory;
import com.exe201.ilink.model.entity.Shop;
import com.exe201.ilink.model.enums.ProductSort;
import com.exe201.ilink.model.enums.ProductStatus;
import com.exe201.ilink.model.exception.ILinkException;
import com.exe201.ilink.model.payload.request.ProductRequest;
import com.exe201.ilink.model.payload.request.UpdateProductRequest;
import com.exe201.ilink.model.payload.response.ProductResponse;
import com.exe201.ilink.model.payload.response.ShopProductResponse;
import com.exe201.ilink.repository.ProductCategoryRepository;
import com.exe201.ilink.repository.ProductRepository;
import com.exe201.ilink.repository.ShopRepository;
import com.exe201.ilink.service.CloudinaryService;
import com.exe201.ilink.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImplement implements ProductService {

    private final ProductCategoryRepository productCategoryRepository;
    private final ProductRepository productRepository;
    private final ShopRepository shopRepository;
    private final ModelMapper modelMapper;
    private final CloudinaryService cloudinaryService;
    private final GenericConverter<Product> genericConverter;


    @Override
    public void addProduct(ProductRequest product) {
        Shop shop = shopRepository.findById(product.getShopId())
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Product creation fails. Shop not found, please contact the administrator."));

        ProductCategory productCategory = productCategoryRepository.findById(product.getCategoryId())
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Product creation fails. Product category not found, please contact the administrator."));

//        Product newProduct = modelMapper.map(product, Product.class);
        Product newProduct = genericConverter.toDTO(product, Product.class);

        newProduct.setShop(shop);
        newProduct.setCategory(productCategory);
        newProduct.setStatus(ProductStatus.PENDING.name());
        productRepository.save(newProduct);

    }

    @Override
    public void updateProduct(Long productId, UpdateProductRequest product) {
        Product productToUpdate = productRepository.findById(productId)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Product update fails. Product not found, please contact the administrator."));

        productToUpdate.setProductName(product.getProductName());
        productToUpdate.setDescription(product.getDescription());
        productToUpdate.setPrice(product.getPrice());
        productToUpdate.setStock(product.getStock());

        productRepository.save(productToUpdate);
    }

    @Override
    public ShopProductResponse getShopProducts(Long shopId, int pageNo, int pageSize, ProductSort sortBy, Double minPrice, Double maxPrice, String keyword) {

        Shop shop = shopRepository.findById(shopId)
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Product creation fails. Shop not found, please contact the administrator."));

        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);

        Specification<Product> spec = Specification.where(ProductSpecification.hasShopId(shopId))
            .and(Specification.where(
                ProductSpecification.hasPrice(minPrice, maxPrice)));

        if (keyword != null) {
            spec = Specification.where(ProductSpecification.hasShopId(shopId)) //Điều kiện tiên quyết
                .and( //Điều kiện kết hợp
                    Specification.where(ProductSpecification.hasProdName(keyword))
                        .or(ProductSpecification.hasCateName(keyword))
                        .or(ProductSpecification.hasPrice(minPrice, maxPrice))
                );
        }

        Page<Product> productContent = productRepository.findAll(spec, pageable);
        return getProductResponse(productContent);

    }

    @Override
    public ShopProductResponse getAllOrSearchProducts(int pageNo, int pageSize, ProductSort sortBy, String keyword, Double minPrice, Double maxPrice) {

        Sort sort = Sort.by(sortBy.getDirection(), sortBy.getField());
        Pageable pageable = PageRequest.of(pageNo, pageSize, sort);
        Specification<Product> spec = Specification.where(ProductSpecification.hasProdName(keyword))
            .or(ProductSpecification.hasCateName(keyword))
            .or(ProductSpecification.hasShopName(keyword))
            .or(ProductSpecification.hasPrice(minPrice, maxPrice));

        Page<Product> productContent = productRepository.findAll(spec, pageable);
        return getProductResponse(productContent);
    }

    @Override
    public ProductResponse getProductDetails(Long productId) {

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ILinkException(HttpStatus.INTERNAL_SERVER_ERROR, "Product retrieve fails. Product not found, please contact the administrator."));

        return modelMapper.map(product, ProductResponse.class);
    }

    @Override
    @Transactional
    public void addPicture(Long productId, MultipartFile file) throws IOException {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Product picture update fails. Product not found, please contact the administrator."));

        String imgUrl = cloudinaryService.uploadFile(file);

        product.setImage(imgUrl);
        productRepository.save(product);
    }

    @Override
    public void manageProduct(Long productId, String status) {
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ILinkException(HttpStatus.BAD_REQUEST, "Product manage fails. Product not found, please contact the administrator."));

        if (!ProductStatus.isContains(status)) {
            throw new ILinkException(HttpStatus.BAD_REQUEST, "Product manage fails. Invalid status, please contact the administrator.");
        }
        product.setStatus(status.toUpperCase());
        productRepository.save(product);

    }

    private ShopProductResponse getProductResponse(Page<Product> productContent) {
        List<Product> products = productContent.getContent();
        List<ProductResponse> content = new ArrayList<>();

        products.forEach(prod -> {
            ProductResponse productResponse = modelMapper.map(prod, ProductResponse.class);
            productResponse.setShopId(prod.getShop().getShopId());
            productResponse.setProductId(prod.getId());
            productResponse.setCategoryName(prod.getCategory().getName());
            productResponse.setShopName(prod.getShop().getShopName());
            content.add(productResponse);
        });

        return new ShopProductResponse(
            content,
            productContent.getNumber(),
            productContent.getSize(),
            productContent.getTotalElements(),
            productContent.getTotalPages(),
            productContent.isLast()
        );
    }


}
