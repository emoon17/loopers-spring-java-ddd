package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeSummaryModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LikeSummaryJpaRepository extends JpaRepository<LikeSummaryModel, Long> {

    List<LikeSummaryModel> findLikeCountByProductIdIn(List<String> productId);

}
