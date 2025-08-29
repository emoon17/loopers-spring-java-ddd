package com.loopers.domain.userCoupon.event;

public record UseCouponCommand(
        String loginId,
        String userCouponId
) {
}
