package com.loopers.application.like;

import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.LikeSummaryModel;
import com.loopers.domain.like.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class LikeCountUpdater {

    private final LikeService likeService;
    private final LikeRepository likeRepository;

    /**
     * LikeEvent 발생 시 집계 업데이트
     * 메인 트랜잭션이 커밋된 후(AFTER_COMMIT)에 실행되므로
     * Like 저장이 실패하면 집계도 반영되지 않음.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(LikeEvent event){
        LikeSummaryModel summary = likeService
                .getLikeCountByProductIdWithLock(event.productId());

        if (summary == null) {
            summary = new LikeSummaryModel(event.productId(), 0);
        }

        if(event.action() == LikeEvent.Action.LIKE){
            summary.increase();
        } else {
            summary.decrease();
        }

        likeRepository.saveLikeSummary(summary);

    }
}
