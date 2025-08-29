package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointHistoryModel;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class PointRepositoryImpl implements PointRepository {
    private final PointJpaRepository pointJpaRepository;
    private final PointHistoryJpaRepository pointHistoryJpaRepository;

    @Override
    public PointModel save(PointModel point) {
        return pointJpaRepository.save(point);
    }

    @Override
    public PointModel findPointByLoginId(String loginId) {
        return pointJpaRepository.findPointByLoginId(loginId)
                .orElse(null);
    }

    @Override
    public Optional<PointModel> findPointByLoginIdWithLock(String loginId) {
        return pointJpaRepository.findByLoginIdWithLock(loginId);
    }

    @Override
    public void savePointHistory(PointHistoryModel pointHistoryModel) {
        pointHistoryJpaRepository.save(pointHistoryModel);
    }

    @Override
    public Optional<PointHistoryModel> findByOrderIdAndReason(String orderId, String reason) {
        return pointHistoryJpaRepository.findByOrderIdAndReason(orderId, reason);
    }
}
