package com.loopers.domain.brand;

import com.loopers.domain.product.ProductModel;

import java.util.List;
import java.util.Optional;

public interface BrandRepository {
    List<BrandModel> findAllById(List<String> brandId);
    Optional<BrandModel> findBrandByProductId(String productId);
}
