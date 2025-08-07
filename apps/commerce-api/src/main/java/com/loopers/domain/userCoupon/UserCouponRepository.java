package com.loopers.domain.userCoupon;

import java.util.Optional;

public interface UserCouponRepository {

    Optional<UserCouponModel> findUserCoupon(String userCouponId);
}
