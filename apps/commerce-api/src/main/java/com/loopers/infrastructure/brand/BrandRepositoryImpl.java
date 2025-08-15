package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.product.ProductModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class BrandRepositoryImpl implements BrandRepository {
    private final BrandJpaRepository jpaRepository;

    @Override
    public List<BrandModel> findAllById(List<String> brandId) {
        return jpaRepository.findAllByBrandIdIn(brandId);
    }

    @Override
    public Optional<BrandModel> findBrandByProductId(String productId) {
        return jpaRepository.findById(productId);
    }
}
