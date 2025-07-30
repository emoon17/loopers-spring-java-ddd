package com.loopers.domain.product;

import com.loopers.application.product.ProductSortCondition;
import com.loopers.domain.brand.BrandModel;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    public void tearDown() { databaseCleanUp.truncateAllTables(); }

    @DisplayName("상품목록 조회할 때,")
    @Nested
    class GetProducts{
        @DisplayName("상품목록 조회시, 상품들의 전체 목록들을 가져온다.")
        @Test
        void retrunAllProducts_whenGetProducts(){
            // arrange
            productJpaRepository.save(
                    new ProductModel(
                            "product1",
                            "상품1",
                            "상품1임당",
                            "brand1",
                            10000,
                            3
                    )
            );

            productJpaRepository.save(
                    new ProductModel(
                            "product2",
                            "상품2",
                            "상품2임당",
                            "brand2",
                            30000,
                            6
                    )
            );

            // act
            List<ProductModel> result = productService.getAllProducts(ProductSortCondition.PRICE_DESC);

            // assert
            assertThat(result).isNotEmpty();
            assertThat(result.size()).isEqualTo(2);
            assertThat(result.get(0).getProductId()).isEqualTo("product2");
            assertThat(result.get(1).getBrandId()).isEqualTo("brand1");
        }


    }

}
