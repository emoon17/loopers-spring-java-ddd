package com.loopers.application.product;

import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.domain.like.LikeModel;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeSummaryModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
                .map(pwb -> ProductInfo.fromList(
                        pwb.getProduct(),
                        pwb.getBrand().getBrandName(),
                        likeTotalMap.getOrDefault(pwb.getProduct().getProductId(), 0)                ))
                .collect(Collectors.toList());

    }

    public ProductInfo getProduct(ProductModel product, UserModel user) {
        // 1. 상품 조회
        ProductModel productModel = productService.getProduct(product)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "상품이 존재하지 않습니다."));

        // 2. 브랜드 조회
        BrandModel brandModel = brandService.getBrandByProductId(product)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "존재하지 않는 브랜드입니다."));

        // 3. 상품 + 브랜드 조합
        ProductWithBrand productWithBrand = productWithBrandService.toProductWithBrand(productModel, brandModel);

        // 4. 좋아요 갯수 조회
        LikeSummaryModel likeSummaryModel = likeService.getProductLikeSummary(product);

        // 5. 사용자 좋아요 조회
        LikeModel likeModel =  likeService.getLike(product, user).orElse(null);

        // 6. 응답
        return ProductInfo.fromDetail(
                productWithBrand.getProduct(),
                productWithBrand.getBrand().getBrandName(),
                likeSummaryModel.getTotalLikeCount(),
                likeModel != null && likeModel.isLike()
        );

    }
}
