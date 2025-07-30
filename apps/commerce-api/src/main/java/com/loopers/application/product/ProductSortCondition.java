package com.loopers.application.product;

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

    public String getSortField() {
        return sortField;
    }

    public Direction getDirection() {
        return direction;
    }

    public enum Direction {
        ASC, DESC
    }
}

