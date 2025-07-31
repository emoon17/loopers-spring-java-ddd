package com.loopers.application.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.product.ProductModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductWithBrandService {

    public List<ProductWithBrand> toProductWithBrandList(List<ProductModel> products, Map<String, BrandModel> brandMap) {
        return products.stream()
                .map(product -> {
                    BrandModel brand = brandMap.get(product.getBrandId());
                    return toProductWithBrand(product, brand);
                })
                .collect(Collectors.toList());
    }

    public ProductWithBrand toProductWithBrand(ProductModel product, BrandModel brand) {
        return new ProductWithBrand(product, brand);
    }
}
