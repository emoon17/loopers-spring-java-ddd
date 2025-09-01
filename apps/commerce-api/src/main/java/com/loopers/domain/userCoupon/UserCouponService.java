package com.loopers.domain.userCoupon;

import com.loopers.domain.userCoupon.event.UseCouponCommand;
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
public class UserCouponService {
    private final UserCouponRepository userCouponRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public UserCouponModel getUserCouponByUserCouponId(String userCouponId) {
        return userCouponRepository.findUserCouponByUserCouponId(userCouponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "이미 사용된 쿠폰입니다."));

    }

    @Transactional
    public UserCouponModel handle(UseCouponCommand useCouponCommand) {
        UserCouponModel userCoupon = getUserCouponByUserCouponId(useCouponCommand.userCouponId());

        userCoupon.validateOwner(useCouponCommand.loginId());
        userCoupon.use();
        return userCouponRepository.saveUserCoupon(userCoupon);
    }

    @Transactional
    public void restoreCoupon(String userCouponId) {
        UserCouponModel userCoupon = userCouponRepository.findUserCouponByUserCouponId(userCouponId)
                .orElseThrow();
        userCoupon.restore();
        userCouponRepository.saveUserCoupon(userCoupon);
    }
}
