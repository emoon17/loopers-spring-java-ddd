package com.loopers.domain.userCoupon;

import java.util.Optional;

public interface UserCouponRepository {

    Optional<UserCouponModel> findUserCouponByUserCouponId(String userCouponId);
    UserCouponModel saveUserCoupon(UserCouponModel userCoupon);
}
