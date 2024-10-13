package com.exe201.ilink.config.converter;

import com.exe201.ilink.model.entity.Account;
import com.exe201.ilink.model.entity.Product;
import com.exe201.ilink.model.payload.dto.request.ProductRequest;
import com.exe201.ilink.model.payload.dto.response.UpdateAccountResponse;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class GenericConverter<T> {

    private final ModelMapper modelMapper;

    @Autowired
    public GenericConverter(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;

        modelMapper.getConfiguration().setFieldMatchingEnabled(true) // Enable field matching -> MATCH những field có tên giống nhau
            .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE) // Set field access level
            .setAmbiguityIgnored(true) // Ignore ambiguity in property mapping
            .setSkipNullEnabled(false) // Allow mapping null values

        ;



//        modelMapper.typeMap(ProductRequest.class, Product.class).addMappings(mapper -> {
//            mapper.skip(Product::setShop); // Bỏ qua ánh xạ shopId với thuộc tính Shop của Product
//            mapper.skip(Product::setCategory); // Bỏ qua ánh xạ categoryId với thuộc tính Category của Product
//        });

    }

    /*
     * Hàm để updateEntity bằng Object DTO, và trả về loại Object của entity
     *Object DTO: Thông tin mà user muốn thay đổi
     *T entity: kiểu dữ liệu của Entity muốn trả về
     * */
    public T updateEntity(Object dto, T entity) {
        modelMapper.map(dto, entity);
        return entity;
    }

    /* Hàm để convert entity sang DTO
     *Class<T> dtoClass: kiểu dữ liệu của DTO muốn trả về
     *Object entity: Object chứa thông tin của entity để convert sang DTO
     * */
    public T toDTO(Object entity, Class<T> dtoClass) {
        return modelMapper.map(entity, dtoClass);
    }

    /* Hàm để convert  DTO sang entity
     *Class<T> entityClass: kiểu dữ liệu của entity muốn trả về
     *Object dto: Object chứa thông tin của DTO để convert sang entity
     * */
    public T toEntity(Object dto, Class<T> entityClass) {
        return modelMapper.map(dto, entityClass);
    }

}
