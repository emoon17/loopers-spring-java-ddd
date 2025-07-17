package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointRepositoryImpl implements PointRepository {
    private final PointJpaRepository pointJpaRepository;

    @Override
    public PointModel save(PointModel point) {
        return pointJpaRepository.save(point);
    }

    @Override
    public PointModel findPointByLoginId(String loginId) {
        return pointJpaRepository.findPointByLoginId(loginId)
                .orElse(null);
    }
}
