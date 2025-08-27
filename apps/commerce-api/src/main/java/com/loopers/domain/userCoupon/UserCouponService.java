package com.loopers.domain.userCoupon;

import com.loopers.domain.user.UserModel;
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
public class UserCouponService {
    private final UserCouponRepository userCouponRepository;

    @Transactional
    public UserCouponModel getUserCouponByUserCouponId(String userCouponId) {
        return userCouponRepository.findUserCouponByUserCouponId(userCouponId)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "이미 사용된 쿠폰입니다."));

    }

    @Transactional
    public UserCouponModel useCoupon(String loginId, String userCouponId) {
        UserCouponModel userCoupon = getUserCouponByUserCouponId(userCouponId);

        userCoupon.validateOwner(loginId);
        userCoupon.use();

        return userCouponRepository.saveUserCoupon(userCoupon);
    }
}
