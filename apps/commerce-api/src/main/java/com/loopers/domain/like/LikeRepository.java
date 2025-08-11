package com.loopers.domain.like;

import com.loopers.domain.product.ProductModel;

import java.util.List;
import java.util.Optional;

public interface LikeRepository {

    List<LikeSummaryModel> findLikeCountByProductIdIn(List<String> productId);
    Optional<LikeSummaryModel> findLikeCountByProductId(String productId);
    LikeSummaryModel findLikeCountByProductIdWithLock(String productId);
    LikeSummaryModel saveLikeSummary(LikeSummaryModel likeSummaryModel);

    Optional<LikeModel> findByLoginIdAndProductId(String loginId, String productId);
    LikeModel saveLike(LikeModel likeModel);
    int demoteToFalseIfTrue(String loginId, String productId);


}
