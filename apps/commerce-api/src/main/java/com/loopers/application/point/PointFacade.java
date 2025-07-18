package com.loopers.application.point;

import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PointFacade {
    private final PointService pointService;

    public PointInfo getPointInfoByLoginId(String loginId) {
        if(loginId == null || loginId.isEmpty()) {
            throw new CoreException(ErrorType.BAD_REQUEST);
        }
        PointModel pointModel = pointService.getPointModelByLoginId(loginId);
        return PointInfo.from(pointModel);
    }

    public PointInfo chargePoint(String loginId, Long amount) {
        PointModel pointModel = pointService.chargePoint(loginId, amount);
        return PointInfo.from(pointModel);
    }
}
