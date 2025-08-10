package com.loopers.domain.like;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LikeService {
    private final LikeRepository likeRepository;

    @Transactional
    public void like(String loginId, String productId) {
        LikeModel likeModel = likeRepository.findByLoginIdAndProductId(loginId, productId).orElse(null);
        if (likeModel != null && likeModel.isLike()) return;
        if (likeModel == null) {
                likeModel = new LikeModel(UUID.randomUUID().toString(), productId, loginId,true);
                likeRepository.saveLike(likeModel);
        }
        likeModel.like();
        increaseLikeSummary(productId);
    }

    @Transactional
    public void unLike(String loginId, String productId) {
        int updated = likeRepository.demoteToFalseIfTrue(loginId, productId);
        if (updated == 0) return;
        decreaseLikeSummary(productId);
    }

    @Transactional
    public void increaseLikeSummary(String productId) {
        LikeSummaryModel summary = likeRepository.findLikeCountByProductIdWithLock(productId);
        summary.increase();
        likeRepository.saveLikeSummary(summary);
    }

    @Transactional
    public void decreaseLikeSummary(String productId) {
        LikeSummaryModel summary = likeRepository.findLikeCountByProductIdWithLock(productId);
        summary.decrease();
        likeRepository.saveLikeSummary(summary);
    }


    public Optional<LikeModel> getLike(ProductModel productModel, UserModel userModel) {
        // optional 고민
       return likeRepository.findByLoginIdAndProductId(productModel.getProductId(), userModel.getLoginId());
    }

    public List<LikeSummaryModel> getProductLikeSummaries (List<String> productId) {
        return likeRepository.findLikeCountByProductIdIn(productId);
    }

    public LikeSummaryModel getProductLikeSummary (ProductModel productModel) {
        return likeRepository.findLikeCountByProductId(productModel.getProductId()).orElseThrow(
                () -> new CoreException(ErrorType.BAD_REQUEST, "상품이 존재하지 않습니다.")
        );
    }

}
