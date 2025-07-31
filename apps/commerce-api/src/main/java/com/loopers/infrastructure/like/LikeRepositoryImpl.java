package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeModel;
import com.loopers.domain.like.LikeRepository;
import com.loopers.domain.like.LikeSummaryModel;
import com.loopers.domain.product.ProductModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class LikeRepositoryImpl implements LikeRepository {
    private final LikeJpaRepository likeJpaRepository;
    private final LikeSummaryJpaRepository likeSummaryJpaRepository;

    @Override
    public List<LikeSummaryModel> findLikeCountByProductIdIn(List<String> productId) {
        return likeSummaryJpaRepository.findLikeCountByProductIdIn(productId);
    }

    @Override
    public Optional<LikeSummaryModel> findLikeCountByProductId(String productId) {
        return likeSummaryJpaRepository.findLikeCountByProductId(productId);
    }

    @Override
    public Optional<LikeModel> findByLoginIdAndProductId(String loginId, String productId) {
        return likeJpaRepository.findByLoginIdAndProductId(loginId, productId);
    }

    @Override
    public void saveLike(LikeModel likeModel) {
        likeJpaRepository.save(likeModel);
    }

    @Override
    public void deleteLike(LikeModel likeModel) {
        likeJpaRepository.delete(likeModel);
    }

    @Override
    public void saveLikeSummary(LikeSummaryModel likeSummaryModel) {
        likeSummaryJpaRepository.save(likeSummaryModel);
    }


}
