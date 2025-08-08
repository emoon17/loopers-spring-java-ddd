package com.loopers.domain.coupon;

import java.util.Optional;

public interface CouponRepository {
    Optional<CouponModel> findCouponByCouponIdWithLock(String couponId);
}
