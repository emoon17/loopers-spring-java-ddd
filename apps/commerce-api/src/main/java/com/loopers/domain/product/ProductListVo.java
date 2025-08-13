package com.loopers.domain.product;

public record ProductListVo(
        String productId,
        String productName,
        String brandId,
        String brandName,
        Long price,
        Long stock,
        int totalLikeCount
) {}
