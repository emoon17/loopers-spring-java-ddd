package com.loopers.domain.point;

import java.util.Optional;

public interface PointRepository {

    PointModel save(PointModel point);

    PointModel findPointByLoginId(String loginId);

    Optional<PointModel> findPointByLoginIdWithLock(String loginId);

}
