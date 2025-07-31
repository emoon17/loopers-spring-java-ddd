package com.loopers.domain.like;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
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
            increaseLikeSummary(productId);
        } else {
            LikeModel like = likeModel.get();
            if(!like.isLike()){
                like.like();
                increaseLikeSummary(productId);
            }
        }
    }

    public void deleteLike(String loginId, String productId) {
        Optional<LikeModel> optional = likeRepository.findByLoginIdAndProductId(loginId, productId);

        if (optional.isPresent()) {
            LikeModel like = optional.get();
            if (like.isLike()) {
                like.unlike();
                decreaseLikeSummary(productId);
            }
        }
    }

    private void increaseLikeSummary(String productId) {
        LikeSummaryModel summary = likeRepository.findLikeCountByProductId(productId)
                .orElseThrow(() -> new CoreException(ErrorType.INTERNAL_ERROR, "서버 오류가 발생했습니다."));
        summary.increase();
        likeRepository.saveLikeSummary(summary);
    }

    private void decreaseLikeSummary(String productId) {
        LikeSummaryModel summary = likeRepository.findLikeCountByProductId(productId)
                .orElseThrow(() -> new CoreException(ErrorType.INTERNAL_ERROR, "서버 오류가 발생했습니다."));
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
