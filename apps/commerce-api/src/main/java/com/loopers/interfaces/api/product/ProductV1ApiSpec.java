package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductSortCondition;
import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@Tag(name="Product V1 API", description = "상품 관련 API입니다.")
public interface ProductV1ApiSpec {

    @Operation(
            summary = "상품 전체 목록 조회",
            description = "등록된 상품의 전체 목록을 조회합니다."
    )
    ApiResponse<List<ProductV1Dto.ProductResponse>> getProducts(
            String brandName,
            ProductSortCondition sort,
            int page,
            int size
    );

    @Operation(
            summary = "상품 상세 조회",
            description = "등록된 상품의 상세정보를 조회합니다."
    )
    ApiResponse<ProductV1Dto.ProductResponse> getProduct(
            @RequestHeader("X-USER-ID")
            @Schema(description = "유저 식별자 (loginId)", example = "test123")
            String loginId,
            String productId
    );
}
