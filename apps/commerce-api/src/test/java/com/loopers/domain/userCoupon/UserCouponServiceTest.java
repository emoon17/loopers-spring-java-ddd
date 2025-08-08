package com.loopers.domain.userCoupon;

import com.loopers.domain.coupon.CouponService;
import com.loopers.infrastructure.coupon.CouponJpaRepository;
import com.loopers.infrastructure.userCoupon.UserCouponJapRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.shaded.org.checkerframework.checker.units.qual.A;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;

@SpringBootTest
public class UserCouponServiceTest {

    @Autowired
    private UserCouponService userCouponService;

    @Autowired
    private UserCouponJapRepository userCouponJpaRepository;

    @DisplayName("사용자의 쿠폰을 조회할 때")
    @Nested
    class GetUserCoupon {
        @DisplayName("사용자의 쿠폰이 존재하지 않으면, 에러를 반환한다.")
        @Test
        void retrunNotFound_whenUserCouponIsNotExist() {
            // arrange
            UserCouponModel usercoupon = new UserCouponModel(
                    "",
                    "user123",
                    "coupon123",
                    "2025-01-01"
            );

            // act + assert
            CoreException result = assertThrows(CoreException.class, () ->
                    userCouponService.getUserCouponByUserCouponId(usercoupon.getUserCouponId())
            );

            assertThat(result.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);
        }

    }
}
