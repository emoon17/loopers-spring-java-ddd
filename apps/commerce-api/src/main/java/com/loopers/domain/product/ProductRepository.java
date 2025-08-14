package com.loopers.domain.product;

import com.loopers.application.product.ProductSortCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {

    List<ProductModel> findAllProducts(ProductSortCondition sortCondition);
    Optional<ProductModel> findProduct(String productId);
    Optional<ProductModel> findProductByProductId(String productId);
    Optional<ProductModel> findProductByProductIdWithLock(String productId);
    void saveProduct(ProductModel product);
    Page<ProductListVo> findAllProdcutListVo(String brandName, String sort, Pageable pageable);
}
