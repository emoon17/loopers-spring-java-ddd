package com.loopers.domain;

import com.loopers.domain.coupon.CouponModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.loopers.domain.coupon.CouponModel.CouponType.FIXED;
import static com.loopers.domain.coupon.CouponModel.CouponType.PERCENT;
import static com.loopers.domain.coupon.CouponModel.TargetType.PRODUCT;
import static org.junit.Assert.assertEquals;

public class CouponModelTest {

    @DisplayName("쿠폰할인을 계산할 때,")
    @Nested
    class CalculateDiscount {

        @Test
        @DisplayName("정액 쿠폰일 경우, 고정된 할인 금액을 반환한다.")
        void returnFixedAmount_whenFixedCoupon(){
                // arrange
                CouponModel coupon = new CouponModel(
                        "coupon001",
                        FIXED,
                        5000L,
                        null,
                        PRODUCT,
                        "product001",
                        "2025-01-01",
                        "2025-12-31"
                );

                // act
                Long discount = coupon.calculateDiscount(30000L);

                // assert
                assertEquals(Long.valueOf(5000L) , discount);

        }
        @Test
        @DisplayName("정률 쿠폰일 경우, 총 금액의 할인율만큼 할인된다")
        void returnPercentAmount_whenPercentCoupon() {
            // arrange
            CouponModel coupon = new CouponModel(
                    "coupon002",
                    PERCENT,
                    null,
                    0.1, // 10%
                    PRODUCT,
                    "product001",
                    "2025-01-01",
                    "2025-12-31"
            );

            // act
            Long discount = coupon.calculateDiscount(10000L); // 기대: 1000

            // assert
            assertEquals(Long.valueOf(1000L), discount);
        }

    }
}
