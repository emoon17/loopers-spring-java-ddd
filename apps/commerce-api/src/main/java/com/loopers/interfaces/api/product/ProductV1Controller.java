package com.loopers.interfaces.api.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductInfo;
import com.loopers.application.product.ProductSortCondition;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.*;

import java.awt.print.Pageable;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/products")
public class ProductV1Controller implements ProductV1ApiSpec{

    private final ProductFacade productFacade;


    @GetMapping
    @Override
    public ApiResponse<List<ProductV1Dto.ProductResponse>> getProducts(
            @RequestParam(required = false) String brandName,
            @RequestParam(defaultValue = "LATEST") ProductSortCondition sort,
            @RequestParam(defaultValue="0") int page,
            @RequestParam(defaultValue="20") int size
    ) {
        PageRequest pageable = PageRequest.of(page, size);
        List<ProductInfo> productInfos = productFacade.getProductList(brandName, sort.name(), pageable);
        List<ProductV1Dto.ProductResponse> responses = productInfos.stream()
                .map(ProductV1Dto.ProductResponse::from)
                .toList();
        return ApiResponse.success(responses);
    }

    @GetMapping("/{productId}")
    @Override
    public ApiResponse<ProductV1Dto.ProductResponse> getProduct(
            @RequestHeader(value = "X-USER-ID", required = false) String loginId,
            @PathVariable String productId
    ) {
        ProductInfo productInfo = productFacade.getProduct(productId, loginId);
        return ApiResponse.success(ProductV1Dto.ProductResponse.from(productInfo));
    }
}
