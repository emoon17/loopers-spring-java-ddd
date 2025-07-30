package com.loopers.application.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeSummaryModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class ProductFacade {
    private final ProductService productService;
    private final BrandService brandService;
    private final ProductWithBrandService productWithBrandService;
    private final LikeService likeService;

    public List<ProductInfo> getProductList(ProductSortCondition sortCondition) {
        // 1. 상품 조회
        List<ProductModel> products = productService.getAllProducts(sortCondition);
        // 2. 브랜드 조회
        Map<String, BrandModel> brandMap =
                brandService.getBrandsById(products.stream()
                        .map(ProductModel::getBrandId)
                        .distinct()
                        .toList()
                ).stream().collect(Collectors.toMap(
                        BrandModel::getBrandId, Function.identity()
                ));
        // 3. 상품 + 브랜드 조합
        List<ProductWithBrand> productWithBrandList =
                productWithBrandService.toProductWithBrandList(products, brandMap);

        // 4. 좋아요 갯수 조회
        Map<String, Integer> likeTotalMap = likeService.getProductLikeSummaries(products.stream()
                .map(ProductModel::getProductId)
                .toList())
                .stream().collect(Collectors.toMap(
                        LikeSummaryModel::getProductId,
                        LikeSummaryModel::getTotalLikeCount
                ));

        // 5. 응답 info에 세팅
        return productWithBrandList.stream()
                .map(pwb -> ProductInfo.from(
                        pwb.getProduct(),
                        pwb.getBrand().getBrandName(),
                        likeTotalMap.getOrDefault(pwb.getProduct().getProductId(), 0)                ))
                .collect(Collectors.toList());

    }

    public ProductInfo getProduct() {

    }
}
