package com.loopers.infrastructure.like;

import com.loopers.domain.like.LikeModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LikeJpaRepository extends JpaRepository<LikeModel, Long> {
    Optional<LikeModel> findByLoginIdAndProductId(String loginId, String productId);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        UPDATE LikeModel l
        SET l.isLike = false
        WHERE l.loginId = :loginId AND l.productId = :productId and l.isLike = true
""")
    int demoteToFalseIfTrue(@Param("loginId") String loginId, @Param("productId") String productId);

}
