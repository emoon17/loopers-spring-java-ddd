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
            List<UserModel> users = List.of(
                    new UserModel("user1", "example@text.com",   "2000-01-01", "W"),
                    new UserModel("user2", "example@text.com",    "2000-01-01", "W"),
                    new UserModel("user3", "example@text.com",   "2000-01-01", "W"),
                    new UserModel("user4", "example@text.com",    "2000-01-01", "W"),
                    new UserModel("user5", "example@text.com",    "2000-01-01", "W")
            );
            List<UserModel> savedUsers = users.stream()
                    .map(userJpaRepository::save)
                    .toList();

            productService.saveProduct(
                    new ProductModel(
                            "p001",
                            "운동화",
                            "런닝 운동화",
                            "b001",
                            1000L,
                            5L
                    )
            );

            LikeSummaryModel likeSummary = likeSummaryJpaRepository.save(
                    new LikeSummaryModel(
                            "p001",
                            0
                    )
            );



            // act 1: 5명이 동시에 좋아요
            int threads1 = savedUsers.size();
            ExecutorService pool1 = Executors.newFixedThreadPool(threads1);
            CountDownLatch ready1 = new CountDownLatch(threads1);
            CountDownLatch start1 = new CountDownLatch(1);
            CountDownLatch done1 = new CountDownLatch(threads1);

            for (UserModel u : savedUsers) {
                pool1.submit(() -> {
                    try {
                        ready1.countDown();
                        start1.await();
                        likeService.createLike(u.getLoginId(), "p001");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done1.countDown();
                    }
                });
            }
            if (!ready1.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Phase1 threads not ready");
            start1.countDown();
            done1.await(10, TimeUnit.SECONDS);
            pool1.shutdownNow();


            // then 1: 좋아요 수 = 5
            likeSummary = likeSummaryJpaRepository.findLikeCountByProductId("p001").orElse(null);
            int totalCount = likeSummary.getTotalLikeCount();
            assertThat(totalCount).isEqualTo(5);

            // act 2: 3명이 동시에 싫어요(취소) — user1, user2, user3
            List<UserModel> toUnlike = savedUsers.subList(0, 3);
            int threads2 = toUnlike.size();
            ExecutorService pool2 = Executors.newFixedThreadPool(threads2);
            CountDownLatch ready2 = new CountDownLatch(threads2);
            CountDownLatch start2 = new CountDownLatch(1);
            CountDownLatch done2 = new CountDownLatch(threads2);

            for (UserModel u : toUnlike) {
                pool2.submit(() -> {
                    try {
                        ready2.countDown();
                        start2.await();
                        likeService.deleteLike(u.getLoginId(), "p001");
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    } finally {
                        done2.countDown();
                    }
                });
            }
            if (!ready2.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("Phase2 threads not ready");
            start2.countDown();
            done2.await(10, TimeUnit.SECONDS);
            pool2.shutdownNow();

            // then 2: 최종 좋아요 수 = 10 - 3 = 7
            likeSummary = likeSummaryJpaRepository.findLikeCountByProductId("p001").orElse(null);
            assertThat(likeSummary.getTotalLikeCount()).isEqualTo(2);
        }
    }
}
