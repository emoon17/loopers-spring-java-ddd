package com.loopers.domain.point;

import com.loopers.domain.point.event.UsePointCommand;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserRepository;
import com.loopers.domain.useraction.UserActionEvent;
import com.loopers.domain.useraction.UserActionType;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class PointService {

    private final PointRepository pointRepository;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;


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
    public Long handle(UsePointCommand command) {
        PointModel pointModel = pointRepository.findPointByLoginIdWithLock(command.loginId())
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND));
        pointModel.usePoint(command.useAmount());

        pointRepository.save(pointModel);
        pointRepository.savePointHistory(PointHistoryModel.used(command.orderId(), command.loginId(), command.useAmount()));

        return pointModel.applyToPayment(command.totalPriceBefore(), command.useAmount());
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
