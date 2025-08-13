package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSortCondition;

public class ProductV1Dto {
//    public record ProductListRequest(
//            ProductSortCondition sort
//    ){
//        public static ProductListRequest of(ProductSortCondition sort) {
//            return new ProductListRequest(
//                    sort == null? ProductSortCondition.LATEST : sort
//            );
//        }
//    }

    public record ProductResponse(
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
        public static ProductResponse from(ProductInfo info) {
            return new ProductResponse(
                    info.productId(),
                    info.productName(),
                    info.description(),
                    info.brandId(),
                    info.price(),
                    info.stock(),
                    info.brandName(),
                    info.likeCount(),
                    info.isLike()
            );
        }
    }
}
