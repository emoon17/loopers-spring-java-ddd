package com.loopers.application.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.product.ProductModel;
import lombok.Getter;

@Getter
public class ProductWithBrand {
    private ProductModel product;
    private BrandModel brand;

    public ProductWithBrand(ProductModel product, BrandModel brand) {
        this.product = product;
        this.brand = brand;
    }

}
