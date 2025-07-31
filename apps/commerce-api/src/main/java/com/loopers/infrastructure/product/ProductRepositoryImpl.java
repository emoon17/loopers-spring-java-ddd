package com.loopers.infrastructure.product;

import com.loopers.application.product.ProductSortCondition;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import com.loopers.support.error.CoreException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class ProductRepositoryImpl implements ProductRepository {
   private final ProductJpaRepository productJpaRepository;

    @Override
    public List<ProductModel> findAllProducts(ProductSortCondition sortCondition) {
        Sort.Direction direction = Sort.Direction.valueOf(sortCondition.getDirection().name());
        String sortField = sortCondition.getSortField();

        Sort sort = Sort.by(direction, sortField);
        return productJpaRepository.findAll(sort);    }

    @Override
    public Optional<ProductModel> findProduct(ProductModel product) {
        return productJpaRepository.findById(product.getProductId());
    }
}
