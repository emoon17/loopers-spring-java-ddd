package com.loopers.domain.product;

import com.loopers.application.product.ProductSortCondition;
import com.loopers.domain.brand.BrandModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<ProductModel> getAllProducts(ProductSortCondition sortCondition) {
        return productRepository.findAllProducts(sortCondition);
    };

//    public ProductModel getProductById(Long id) {
//
//    }

}
