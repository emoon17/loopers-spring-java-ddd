package com.loopers.domain.point;

public interface PointRepository {

    PointModel save(PointModel point);
    PointModel findPointByLoginId(String loginId);
}
