package com.exe201.ilink.config.converter;

import com.exe201.ilink.model.enums.ProductSort;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ProductSortConverter implements Converter<String, ProductSort> {
    @Override
    public ProductSort convert(String source) {
        switch (source.toLowerCase()) {
            case "lowestprice":
                return ProductSort.SORT_BY_PRICE_ASC;
            case "highestprice":
                return ProductSort.SORT_BY_PRICE_DESC;
            case "latestdate":
                return ProductSort.SORT_BY_DATE_ASC;
            case "newestdate":
                return ProductSort.SORT_BY_DATE_DESC;
            default:
                throw new IllegalArgumentException("Invalid sort parameter: " + source);
        }
    }
}
