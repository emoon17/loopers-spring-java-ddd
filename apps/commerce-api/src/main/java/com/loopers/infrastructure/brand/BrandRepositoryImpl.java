package com.loopers.infrastructure.brand;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class BrandRepositoryImpl implements BrandRepository {
    private final BrandJpaRepository jpaRepository;

    @Override
    public List<BrandModel> findAllById(List<String> brandId) {
        return jpaRepository.findAllByBrandIdIn(brandId);
    }
}
