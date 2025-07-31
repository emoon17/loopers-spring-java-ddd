package com.loopers.domain.brand;

import com.loopers.domain.product.ProductModel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BrandService {

    private final BrandRepository brandRepository;

    public List<BrandModel> getBrandsById(List<String> brandId) {
        return brandRepository.findAllById(brandId);
    }

    public Optional<BrandModel> getBrandByProductId(ProductModel product){
        return brandRepository.findBrandByProductId(product);
    }
}
