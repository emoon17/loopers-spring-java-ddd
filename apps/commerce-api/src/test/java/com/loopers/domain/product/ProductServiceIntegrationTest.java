package com.loopers.domain.product;

import com.loopers.application.product.*;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.like.LikeModel;
import com.loopers.domain.like.LikeSummaryModel;
import com.loopers.domain.user.UserModel;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.infrastructure.like.LikeSummaryJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
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
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
public class ProductServiceIntegrationTest {

    @Autowired
    private ProductFacade productFacade;

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductWithBrandService productWithBrandService;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @Autowired
    private LikeSummaryJpaRepository likeSummaryJpaRepository;

    @Autowired
    private LikeJpaRepository likeJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

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
    @DisplayName("상품, 좋아요, 브랜드 등이 담긴 상품 전체 리스트가 반환된다.")
    @Test
    void returnAllProductInfoList_whenGetProductList() {
        // arrange
        BrandModel brand = brandJpaRepository.save(new BrandModel("B001", "나이키"));
        brandJpaRepository.flush();

        ProductModel product1 = productJpaRepository.save(new ProductModel("P001", "신발", "편한 신발", brand.getBrandId(), 10000, 5));
        ProductModel product2 = productJpaRepository.save(new ProductModel("P002", "운동화", "빠른 신발", brand.getBrandId(), 20000, 3));
        productJpaRepository.flush();

        likeSummaryJpaRepository.save(new LikeSummaryModel(product1.getProductId(), 5));
        likeSummaryJpaRepository.save(new LikeSummaryModel(product2.getProductId(), 0));
        likeSummaryJpaRepository.flush();

        // act
        List<ProductModel> products = productService.getAllProducts(ProductSortCondition.PRICE_DESC);

        List<String> brandIds = products.stream()
                .map(ProductModel::getBrandId)
                .distinct()
                .toList();
        Map<String, BrandModel> brandMap = brandJpaRepository.findAllById(brandIds).stream()
                .collect(Collectors.toMap(BrandModel::getBrandId, Function.identity()));
        for (ProductModel p : products) {
            System.out.println("상품 ID: " + p.getProductId() + " / brandId: " + p.getBrandId());
        }
        System.out.println("brandMap keys: " + brandMap.keySet());

        List<ProductWithBrand> productWithBrands = productWithBrandService.toProductWithBrandList(products, brandMap);

        List<String> productIds = products.stream()
                .map(ProductModel::getProductId)
                .toList();
        Map<String, Integer> likeMap = likeSummaryJpaRepository.findLikeCountByProductIdIn(productIds).stream()
                .collect(Collectors.toMap(
                        LikeSummaryModel::getProductId,
                        LikeSummaryModel::getTotalLikeCount
                ));

        List<ProductInfo> result = productWithBrands.stream()
                .map(pwb -> ProductInfo.fromList(
                        pwb.getProduct(),
                        pwb.getBrand().getBrandName(),
                        likeMap.getOrDefault(pwb.getProduct().getProductId(), 0)
                ))
                .toList();

        // assert
        assertThat(result).hasSize(2);
        assertThat(result.get(0).productId()).isEqualTo("P002"); // 가격 높은 순 정렬
        assertThat(result.get(0).brandName()).isEqualTo("나이키");
        assertThat(result.get(0).likeCount()).isEqualTo(0);
        assertThat(result.get(1).likeCount()).isEqualTo(5);
    }

    @DisplayName("상품 상세 조회시, 브랜드, 좋아요 요약, 사용자 좋아요 포함 응답이 반환된다.")
    @Test
    void returnProductInfoWithBrandAndLikes_whenGetProductDetail() {
        // arrange
        BrandModel brand = brandJpaRepository.save(new BrandModel("B001", "나이키"));
        brandJpaRepository.flush();

        // 2. 상품 저장
        ProductModel product = productJpaRepository.save(
                new ProductModel("P001", "운동화", "편한 운동화", brand.getBrandId(), 50000, 10)
        );
        productJpaRepository.flush();

        // 3. 유저 저장
        UserModel user = userJpaRepository.save(
                new UserModel("U001", "testuser@email.com", "1999-01-01", "W")
        );
        userJpaRepository.flush();

        // 4. 좋아요 요약 저장
        likeSummaryJpaRepository.save(new LikeSummaryModel(product.getProductId(), 7));

        // 5. 사용자 좋아요 저장
        LikeModel like = new LikeModel("L001", product.getProductId(), user.getLoginId(), true);
        likeJpaRepository.save(like);
        likeJpaRepository.flush();

        // 6. 조합 로직 (퍼사드 대신 수동 구성)
        ProductWithBrand productWithBrand = new ProductWithBrand(product, brand);
        LikeSummaryModel likeSummaryModel = likeSummaryJpaRepository.findLikeCountByProductId(product.getProductId())
                .orElseThrow();
        LikeModel likeModel = likeJpaRepository.findByLoginIdAndProductId(user.getLoginId(), product.getProductId()).orElse(null);

        // Act
        ProductInfo result = ProductInfo.fromDetail(
                productWithBrand.getProduct(),
                productWithBrand.getBrand().getBrandName(),
                likeSummaryModel.getTotalLikeCount(),
                likeModel != null && likeModel.isLike()
        );

        // assert
        assertThat(result).isNotNull();
        assertThat(result.productId()).isEqualTo("P001");
        assertThat(result.productName()).isEqualTo("운동화");
        assertThat(result.brandName()).isEqualTo("나이키");
        assertThat(result.likeCount()).isEqualTo(7);
        assertThat(result.isLike()).isTrue();
    }




}
