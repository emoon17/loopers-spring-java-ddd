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
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CouponFacade {
    private final CouponService couponService;
    private final UserCouponService userCouponService;

    public void applyCoupon(UserModel user, List<OrderItemModel> orderItems, UserCouponModel userCouponModel) {
        // 1. 유저 쿠폰 조회
        UserCouponModel userCoupon = userCouponService.getUserCoupon(userCouponModel);

        // 2. 유저 검증
        userCoupon.validateOwner(user);

        // 3. 사용 여부 확인
        userCoupon.use();

        // 4. 쿠폰 조회
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
