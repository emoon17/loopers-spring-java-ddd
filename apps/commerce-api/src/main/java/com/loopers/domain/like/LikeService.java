package com.loopers.domain.like;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;

    public void createLike(String loginId, String productId) {
        Optional<LikeModel> likeModel = likeRepository.findByLoginIdAndProductId(loginId, productId);

        if(likeModel.isEmpty()) {
            LikeModel like = new LikeModel(
                    UUID.randomUUID().toString(),
                    productId,
                    loginId,
                    true
            );
            likeRepository.saveLike(like);
            likeRepository.increaseLikeSummary(productId);
        } else {
            LikeModel like = likeModel.get();
            if(!like.isLike()){
                like.like();
                likeRepository.increaseLikeSummary(productId);
            }
        }
    }

    public void deleteLike(String loginId, String productId) {
        Optional<LikeModel> optional = likeRepository.findByLoginIdAndProductId(loginId, productId);

        if (optional.isPresent()) {
            LikeModel like = optional.get();
            if (like.isLike()) {
                like.unlike();
                likeRepository.decreaseLikeSummary(productId);
            }
        }
    }

    public List<LikeSummaryModel> getProductLikeSummaries (List<String> productId) {
        return likeRepository.findLikeCountByProductIdIn(productId);
    }

}
