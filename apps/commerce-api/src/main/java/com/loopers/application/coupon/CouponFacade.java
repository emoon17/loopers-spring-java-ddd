package com.loopers.application.coupon;

import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.userCoupon.UserCouponModel;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.userCoupon.UserCouponService;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CouponFacade {
    private final CouponService couponService;
    private final UserCouponService userCouponService;

    @Transactional
    public void applyCoupon(UserModel user, List<OrderItemModel> orderItems, String userCouponId) {
        UserCouponModel userCoupon;
        try {
            userCoupon = userCouponService.useCoupon(user, userCouponId);
        } catch (org.springframework.orm.ObjectOptimisticLockingFailureException | jakarta.persistence.OptimisticLockException e) {
            // 다른 스레드가 먼저 쿠폰을 사용함 → 비즈니스 의미로 변환
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }
        CouponModel coupon = couponService.getCouponbyCouponId(userCoupon.getCouponId());
        // 5. 할인 대상 여부 확인 (상품ID or 브랜드ID 일치) -- todo. orderItme(productId -> targetId로 변경)
//        boolean isApplicable = orderItems.stream().anyMatch(item ->
//                switch (coupon.getTargetType()) {
//                    case PRODUCT -> coupon.getTargetId().equals(item.getProductId());
//                    case BRAND -> coupon.getTargetId().equals(item.getBrandId());
//                });

        orderItems.stream()
                .filter(item -> coupon.getTargetId().equals(item.getProductId()))
                .findAny()
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "쿠폰을 적용할 수 있는 상품이 없습니다."));

        // 6. 총 주문금액 계산
//        long totalPrice = orderItems.stream().mapToLong(OrderItemModel::getPrice).sum();
//
//        // 7. 할인 금액 계산
//        long discountPrice = coupon.calculateDiscount(totalPrice);
//
//        // 8. 결과 생성
//        return UserCouponInfo.from(userCoupon, coupon, discountPrice);
    }
}
