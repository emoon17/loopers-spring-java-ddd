package com.loopers.domain.like;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserModel;
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
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.N;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Slf4j
@SpringBootTest
public class LikeServiceInteIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private ProductService productService;

    @Autowired
    private LikeJpaRepository likeJpaRepository;

    @Autowired
    private LikeSummaryJpaRepository likeSummaryJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private ProductJpaRepository productJpaRepository;

    @AfterEach
    public void tearDown() { databaseCleanUp.truncateAllTables();}

    @DisplayName("좋아요를 조회할 때,")
    @Nested
    class GetLikeTotalCount {
        @DisplayName("좋아요가 눌린 상품들을 조회할 경우 , 상품들의 좋아요 전체 카운트가 반환된다.")
        @Test
        void returnTotalCount_whenGetProductLikeSummaries() {
            //arrange
            LikeSummaryModel likeSummaryModel1 = likeSummaryJpaRepository.save(
                    new LikeSummaryModel(
                            "p001",
                            50000
                    )
            );

            LikeSummaryModel likeSummaryModel2 = likeSummaryJpaRepository.save(
                    new LikeSummaryModel(
                            "p002",
                            20000
                    )
            );

            List<String> productIds = List.of(likeSummaryModel1.getProductId(), likeSummaryModel2.getProductId());

            //act
            List<LikeSummaryModel> likeSummaries = likeSummaryJpaRepository.findLikeCountByProductIdIn(productIds);
            Map<String, LikeSummaryModel> summaryMap = likeSummaries.stream()
                    .collect(Collectors.toMap(LikeSummaryModel::getProductId, Function.identity()));

            //assert
            assertThat(summaryMap.get(likeSummaryModel1.getProductId()).getTotalLikeCount()).isEqualTo(50000);
            assertThat(summaryMap.get(likeSummaryModel2.getProductId()).getTotalLikeCount()).isEqualTo(20000);
            assertThat(likeSummaries)
                    .extracting(LikeSummaryModel::getProductId, LikeSummaryModel::getTotalLikeCount)
                    .containsExactlyInAnyOrder(
                            tuple("p001", 50000),
                            tuple("p002", 20000)
                    );
        }

        @DisplayName("동일한 상품에 대해 여러명이 좋아요 등록/취소 요청해도, 상품의 좋아요 개수가 정상 반영된다.")
        @Test
        void concurrentLikes_whenIncreaseExactlyOncePerUser() throws InterruptedException {
            // arrange
            String productId = "p001";
            int useCount = 50;

            likeSummaryJpaRepository.save(new LikeSummaryModel(productId, 0));

            ExecutorService es = Executors.newFixedThreadPool(50);
            CountDownLatch start = new CountDownLatch(1);
            CountDownLatch end = new CountDownLatch(useCount);
            for (int i = 0; i < useCount; i++) {
                String loginId = "login" + i;
                es.submit(() -> {
                    try {
                        start.await();
                        likeService.like(loginId, productId);
                    } catch (Exception e) {
                        System.out.println("error::: " + e);
                    } finally {
                        end.countDown();
                    }
                });

            }

            start.countDown();
            end.await();
            es.shutdown();

            // assert
            int summary = likeSummaryJpaRepository.findLikeCountByProductId(productId)
                    .map(LikeSummaryModel::getTotalLikeCount)
                    .orElseThrow(() -> new AssertionError("like_summary 가 존재하지 않음: " + productId));

            assertThat(summary).isEqualTo(useCount);

            // 좋아요 취소
            ExecutorService es2 = Executors.newFixedThreadPool(50);
            CountDownLatch start2 = new CountDownLatch(1);
            CountDownLatch end2 = new CountDownLatch(useCount);
            for (int i = 0; i < useCount; i++) {
                String loginId = "login" + i;
                es2.submit(() -> {
                   try{
                       start2.await();
                       likeService.unLike(loginId, productId);
                   } catch (Exception e) {
                       System.out.println("error::: " + e);
                   } finally {
                       end2.countDown();
                   }
                });
            }
            start2.countDown();
            end2.await();
            es.shutdown();

            int afterUnlike = likeSummaryJpaRepository.findLikeCountByProductId(productId)
                    .map(LikeSummaryModel::getTotalLikeCount).orElse(0);
            assertThat(afterUnlike).isEqualTo(0);


        }


    }
}
