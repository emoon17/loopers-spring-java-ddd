package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeModel;
import com.loopers.domain.like.LikeSummaryModel;
import com.loopers.domain.product.ProductModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LikeSummaryJpaRepository extends JpaRepository<LikeSummaryModel, Long> {

    List<LikeSummaryModel> findLikeCountByProductIdIn(List<String> productId);

    Optional<LikeSummaryModel> findLikeCountByProductId(String productId);


}
