package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.userCoupon.UserCouponModel;

public record UserCouponInfo(
            String userCouponId,
            String couponId,
            CouponModel.CouponType couponType,
            CouponModel.TargetType targetType,
            String targetId,
            Long discountPrice,
            Long discountAmount,
            Double discountRate
    ) {
        public static UserCouponInfo from(UserCouponModel userCoupon, CouponModel coupon, Long discountPrice) {
            return new UserCouponInfo(
                    userCoupon.getUserCouponId(),
                    coupon.getCouponId(),
                    coupon.getCouponType(),
                    coupon.getTargetType(),
                    coupon.getTargetId(),
                    discountPrice,
                    coupon.getDiscountAmount(),
                    coupon.getDiscountRate()
            );
        }

}
