package com.exe201.ilink.model.enums;

import lombok.Getter;
import org.springframework.data.domain.Sort;

@Getter
public enum ProductSort {
    SORT_BY_PRICE_ASC("price", Sort.Direction.ASC),
    SORT_BY_PRICE_DESC("price", Sort.Direction.DESC),
    SORT_BY_DATE_ASC("createdDate", Sort.Direction.ASC),
    SORT_BY_DATE_DESC("createdDate", Sort.Direction.DESC);

    private final String field;
    private final Sort.Direction direction;

    ProductSort(String field, Sort.Direction direction) {
        this.field = field;
        this.direction = direction;
    }
}
