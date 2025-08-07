package com.loopers.domain.userCoupon;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserCouponService {
    private final UserCouponRepository userCouponRepository;

    public UserCouponModel getUserCoupon(UserCouponModel userCouponModel) {
        return userCouponRepository.findUserCoupon(userCouponModel)
                .orElseThrow(() -> new CoreException(ErrorType.NOT_FOUND, "사용자의 쿠폰이 존재하지 않습니다."));

    }
}
