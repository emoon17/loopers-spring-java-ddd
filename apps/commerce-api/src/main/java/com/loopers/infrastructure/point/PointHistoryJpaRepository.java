package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointHistoryModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistoryModel, Long> {

    @Query("""
        select p from PointHistoryModel p where p.orderId = :orderId and p.reason = :reason
""")
    Optional<PointHistoryModel> findByOrderIdAndReason(String orderId, String reason);
}
