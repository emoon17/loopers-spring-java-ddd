package com.loopers.domain.product;

import com.loopers.application.product.ProductSortCondition;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<ProductModel> findAllProducts(ProductSortCondition sortCondition);
    Optional<ProductModel> findProduct(ProductModel product);
    Optional<ProductModel> findProductByProductId(String productId);
    void saveProduct(ProductModel product);
}
