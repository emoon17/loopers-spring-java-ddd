package com.loopers.domain.like;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {

    List<LikeSummaryModel> findLikeCountByProductIdIn(List<String> productId);
    Optional<LikeModel> findByLoginIdAndProductId(String loginId, String productId);

    void saveLike(LikeModel likeModel);
    void deleteLike(LikeModel likeModel);

    void increaseLikeSummary(String productId);
    void decreaseLikeSummary(String productId);
}
