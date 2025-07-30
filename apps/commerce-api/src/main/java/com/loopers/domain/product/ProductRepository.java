package com.loopers.domain.product;

import com.loopers.application.product.ProductSortCondition;

import java.util.List;

public interface ProductRepository {

    List<ProductModel> findAllProducts(ProductSortCondition sortCondition);
}
