package com.loopers.application.product;

import com.loopers.domain.product.ProductModel;

public record ProductInfo(
        String productId,
        String productName,
        String description,
        String brandId,
        int price,
        int stock,
        String brandName,
        int likeCount
) {

    public static ProductInfo from(ProductModel product, String brandName, int likeCount) {
        return new ProductInfo(
                product.getProductId(),
                product.getProductName(),
                product.getProductDescription(),
                product.getBrandId(),
                product.getPrice(),
                product.getStock(),
                brandName,
                likeCount
        );
    }
}
