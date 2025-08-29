package com.loopers.domain.coupon;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.userCoupon.UserCouponModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CouponService {
    private final CouponRepository couponRepository;

    @Transactional
    public CouponModel getCouponbyCouponIdWithLock(String couponId) {
        return couponRepository.findCouponByCouponIdWithLock(couponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "쿠폰 정책이 존재하지 않습니다."));
    }

}
