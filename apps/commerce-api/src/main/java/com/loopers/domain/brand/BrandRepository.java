package com.loopers.domain.brand;

import java.util.List;

public interface BrandRepository {
    List<BrandModel> findAllById(List<String> brandId);
}
