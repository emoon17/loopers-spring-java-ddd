package com.loopers.domain.like;

import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.infrastructure.like.LikeSummaryJpaRepository;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

@Slf4j
@SpringBootTest
public class LikeServiceInteIntegrationTest {

    @Autowired
    private LikeService likeService;

    @Autowired
    private LikeJpaRepository likeJpaRepository;

    @Autowired
    private LikeSummaryJpaRepository likeSummaryJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    public void tearDown() { databaseCleanUp.truncateAllTables();}

    @DisplayName("좋아요를 조회할 때,")
    @Nested
    class GetLikeTotalCount {
        @DisplayName("좋아요가 눌린 상품들을 조회할 경우 , 상품들의 좋아요 전체 카운트가 반환된다.")
        @Test
        void returnTotalCount_whenGetProductLikeSummaries () {
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
    }
}
