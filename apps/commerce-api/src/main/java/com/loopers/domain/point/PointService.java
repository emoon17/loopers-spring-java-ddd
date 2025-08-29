package com.loopers.domain.point;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;

    public PointModel getPointModelByLoginId(String loginId) {
        return pointRepository.findPointByLoginId(loginId);
    }

    public PointModel chargePoint(String loginId, Long amount) {
        if(!userRepository.existsByLoginId(loginId)) {
            throw new CoreException(ErrorType.NOT_FOUND);
        }
        PointModel pointModel = pointRepository.findPointByLoginId(loginId);
        pointModel.chargePoint(amount);
        return pointRepository.save(pointModel);
    }

    @Transactional
    public void usePoint(String orderId, String loginId, Long amount) {
        PointModel pointModel = pointRepository.findPointByLoginIdWithLock(loginId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        pointModel.usePoint(amount);

        pointRepository.save(pointModel);
        pointRepository.savePointHistory(PointHistoryModel.used(orderId, loginId, amount));
    }

    @Transactional
    public void restorePoints(String orderId) {
        PointHistoryModel used = pointRepository.findByOrderIdAndReason(orderId, "USE")
                .orElseThrow();
        PointModel point = pointRepository.findPointByLoginId(used.getLoginId());
        point.chargePoint(used.getUsedAmount());
        pointRepository.save(point);
    }
}
