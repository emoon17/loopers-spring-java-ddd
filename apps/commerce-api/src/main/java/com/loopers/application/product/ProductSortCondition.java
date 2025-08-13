package com.loopers.application.product;

import lombok.Getter;

@Getter
public enum ProductSortCondition {
    LATEST("createdAt", Direction.DESC),
    PRICE_ASC("price", Direction.ASC),
    PRICE_DESC("price", Direction.DESC),
    LIKES_ASC("likeCount", Direction.ASC),
    LIKES_DESC("likeCount", Direction.DESC);


    private final String sortField;
    private final Direction direction;

    ProductSortCondition(String sortField, Direction direction) {
        this.sortField = sortField;
        this.direction = direction;
    }

    public enum Direction {
        ASC, DESC
    }
}

