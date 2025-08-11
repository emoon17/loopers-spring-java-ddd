package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeModel;
import com.loopers.domain.like.LikeSummaryModel;
import com.loopers.domain.product.ProductModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LikeSummaryJpaRepository extends JpaRepository<LikeSummaryModel, Long> {

    List<LikeSummaryModel> findLikeCountByProductIdIn(List<String> productId);

    Optional<LikeSummaryModel> findLikeCountByProductId(String productId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM LikeSummaryModel p WHERE p.productId = :productId")
    LikeSummaryModel findLikeCountByProductIdWithLock(@Param("productId")String productId);


}
