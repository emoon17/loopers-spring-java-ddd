package com.loopers.application.like;

import com.loopers.domain.like.LikeModel;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeService;
import com.loopers.domain.like.event.LikeEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@RequiredArgsConstructor
@Component
public class LikeFacade {

    private final LikeService likeService;
    private final LikeRepository likeRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public void like(String productId, String loginId){
        LikeModel like = likeService.getLikeByProductIdAndLoginId(productId, loginId)
                .orElse(new LikeModel(UUID.randomUUID().toString(), productId, loginId, false));
        if(like.isLike()) return;

        // 1. likeEvent 생성
        LikeEvent likeEvent = like.like();

        // 2. 메인로직-  db 저장
        likeRepository.saveLike(like);

        // 3. 부가로직에 이벤트 발행
        if (likeEvent != null) eventPublisher.publishEvent(likeEvent);
    }

    @Transactional
    public void unlike(String productId, String loginId){
        LikeModel like = likeService.getLikeByProductIdAndLoginId(loginId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Like not found"));

        if (!like.isLike()) return;

        LikeEvent event = like.unlike();
        likeRepository.saveLike(like);

        if (event != null) eventPublisher.publishEvent(event);
    }
}
