package com.loopers.domain.product;

import com.loopers.application.product.ProductFacade;
import com.loopers.application.product.ProductWithBrand;
import com.loopers.application.product.ProductWithBrandService;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.brand.BrandService;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class ProductWithBrandServiceIntegrationTest {
    @Autowired
    private ProductService productService;

    @Autowired
    private BrandService brandService;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private ProductWithBrandService productWithBrandService;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @Autowired
    private ProductFacade productFacade;

    @AfterEach
    public void tearDown() { databaseCleanUp.truncateAllTables(); }

    @DisplayName("상품 목록 조회 시 상품과 브랜드명을 조합해서 보여준다.")
    @Test
    void returnAllProductsWithBrands_whenGetProducts(){
        // arrange
        BrandModel brandA = new BrandModel("b001", "나이키");
        BrandModel brandB = new BrandModel("b002", "아디다스");
        brandJpaRepository.save(brandA);
        brandJpaRepository.save(brandB);

        ProductModel product1 = new ProductModel("p001", "운동화", "러닝화임당", "b001", 49000L, 10L);
        ProductModel product2 = new ProductModel("p002", "츄리닝", "삼선츄리닝임당", "b002", 80000L, 3L);
        productJpaRepository.save(product1);
        productJpaRepository.save(product2);

        List<ProductModel> products = productJpaRepository.findAll().stream()
                .sorted(Comparator.comparingLong(ProductModel::getPrice).reversed()) // 가격 내림차순
                .collect(Collectors.toList());

        Map<String, BrandModel> brandsById = brandJpaRepository.findAll().stream()
                .collect(Collectors.toMap(BrandModel::getBrandId, Function.identity()));

        // act
//        List<ProductInfo> result = productFacade.getProductList(ProductSortCondition.PRICE_DESC);
        List<ProductWithBrand> result = productWithBrandService.toProductWithBrandList(products, brandsById);
        // assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getProduct().getProductId()).isEqualTo("p002"); // 높은 가격 먼저
        assertThat(result.get(1).getProduct().getProductId()).isEqualTo("p001");
    }
}
