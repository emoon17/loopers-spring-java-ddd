package com.loopers.application.like;

import com.loopers.domain.like.LikeModel;
import com.loopers.domain.like.LikeSummaryModel;
import com.loopers.domain.like.event.LikeEvent;
import com.loopers.infrastructure.like.LikeJpaRepository;
import com.loopers.infrastructure.like.LikeSummaryJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

@SpringBootTest
public class LikeFacadeIntegrationTest {

    @Autowired
    private LikeFacade likeFacade;
    @Autowired
    private LikeJpaRepository likeJpaRepository;
    @Autowired
    private LikeSummaryJpaRepository  likeSummaryJpaRepository;
    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @MockitoBean
    private LikeCountUpdater likeCountUpdater;

    @AfterEach
    void tearDown() { databaseCleanUp.truncateAllTables(); }

    @DisplayName("좋아요가 성공하고, 좋아요 카운트로직도 성공했을 때")
    @Test
    void 메인로직_좋아요성공_부가로직_카운트성공(){
        //arrange, act
        likeFacade.like("product01", "loginId01");

        //assert
        LikeModel like = likeJpaRepository.findByLoginIdAndProductId("loginId01", "product01").orElseThrow();
        assertThat(like.isLike()).isTrue();

        LikeSummaryModel summary = likeSummaryJpaRepository.findLikeCountByProductId(like.getProductId()).orElseThrow();
        assertThat(summary.getTotalLikeCount()).isEqualTo(1);

    }

    @DisplayName("좋아요가 성공하고, 좋아요카운트로직은 실패했을 때, 좋아요는 성공한다. ")
    @Test
    void 메인로직_좋아요성공_부가로직_카운트실패해도_좋아요는성공한다(){
        // arrange
        doThrow(new RuntimeException("부가 로직 강제 실패"))
                .when(likeCountUpdater).handle(any(LikeEvent.class));

        // act
        likeFacade.like("product01", "loginId01");

        // assert
        LikeModel like = likeJpaRepository.findByLoginIdAndProductId("loginId01", "product01").orElseThrow();
        assertThat(like.isLike()).isTrue(); // 부가로직 실패해도 좋아요 등록되어있어야한다.
    }

    @DisplayName("좋아요가 실패하면 좋아요카운트로직도 실패한다.")
    @Test
    void 메인로직_좋아요가실패하면_부가로직_좋아요카운트로직도_실패한다(){
        // arrange , act
        assertThatThrownBy(()-> {
            likeFacade.unlike("product01", "loginId01");
        }).isInstanceOf(RuntimeException.class);

        Optional<LikeSummaryModel> summary = likeSummaryJpaRepository.findLikeCountByProductId("loginId01");

        // assert
        assertThat(summary).isEmpty();

    }
}
