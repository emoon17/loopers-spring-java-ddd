package com.loopers.application.product;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.loopers.domain.product.ProductListVo;
import com.loopers.domain.product.ProductModel;
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ProductInfo(
        String productId,
        String productName,
        String description,
        String brandId,
        Long price,
        Long stock,
        String brandName,
        int likeCount,
        Boolean isLike
) {

    public static ProductInfo fromList(ProductListVo v) {
        return new ProductInfo(
                v.productId(),
                v.productName(),
                null,
                v.brandId(),
                v.price(),
                v.stock(),
                v.brandName(),
                v.totalLikeCount(),
                false
        );
    }

    public static ProductInfo fromDetail(ProductModel product, String brandName, int likeCount, boolean isLike) {
        return new ProductInfo(
                product.getProductId(),
                product.getProductName(),
                product.getProductDescription(),
                product.getBrand().getBrandName(),
                product.getPrice(),
                product.getStock(),
                brandName,
                likeCount,
                isLike
        );
    }
}
