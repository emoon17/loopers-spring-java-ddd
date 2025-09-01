package com.loopers.domain.userCoupon;

import com.loopers.domain.userCoupon.event.UseCouponCommand;
import com.loopers.infrastructure.userCoupon.UserCouponJapRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
            UserCouponModel userCoupon = new UserCouponModel("userCouponId01", "loginId01", "coupon-001", "2025-01-01");
            userCouponJpaRepository.save(userCoupon);

            UseCouponCommand command = new UseCouponCommand(userCoupon.getLoginId(), userCoupon.getUserCouponId());

            // act
            UserCouponModel updated = userCouponService.handle(command);

            // assert
            assertThat(updated.isUsed()).isTrue();

            UserCouponModel reloaded = userCouponJpaRepository.findUserCouponByUserCouponId("userCouponId01").orElseThrow();
            assertThat(reloaded.isUsed()).isTrue();
        }

    }

    @Test
    @DisplayName("UseCouponCommand 처리 → 쿠폰 사용 상태로 변경된다")
    void handle_success() {
        // arrange
        UseCouponCommand command = new UseCouponCommand("loginId01", "userCouponId01");

        // act
        UserCouponModel updated = userCouponService.handle(command);

        // assert
        assertThat(updated.isUsed()).isTrue();

        UserCouponModel reloaded = userCouponJpaRepository.findUserCouponByUserCouponId("userCouponId01").orElseThrow();
        assertThat(reloaded.isUsed()).isTrue();
    }
}
