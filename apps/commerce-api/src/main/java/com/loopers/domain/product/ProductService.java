package com.loopers.domain.product;

import com.loopers.application.product.ProductSortCondition;
import com.loopers.domain.brand.BrandModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public List<ProductModel> getAllProducts(ProductSortCondition sortCondition) {
        return productRepository.findAllProducts(sortCondition);
    };

    public Optional<ProductModel> getProduct(ProductModel product) {
        return productRepository.findProduct(product);
    }

    public Optional<ProductModel> getProductByProductId(String productId) {
        return productRepository.findProductByProductId(productId);
    }

    public void saveProduct(ProductModel product) {
        productRepository.saveProduct(product);
    }

}
