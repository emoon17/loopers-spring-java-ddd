package com.loopers.infrastructure.product;

import com.loopers.application.product.ProductSortCondition;
import com.loopers.domain.product.ProductListVo;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Optional<ProductModel> findProduct(String productId) {
        return productJpaRepository.findById(productId);
    }

    @Override
    public Optional<ProductModel> findProductByProductId(String productId) {
        return productJpaRepository.findProductByProductId(productId);
    }

    @Override
    public Optional<ProductModel> findProductByProductIdWithLock(String productId) {
        return productJpaRepository.findProductByProductIdWithLock(productId);
    }

    @Override
    public void saveProduct(ProductModel product) {
        productJpaRepository.save(product);
    }

    @Override
    public Page<ProductListVo> findAllProdcutListVo(String brandId, String sort, Pageable pageable) {
        return productJpaRepository.findAllProdcutListVo(brandId, sort, pageable);
    }
}
