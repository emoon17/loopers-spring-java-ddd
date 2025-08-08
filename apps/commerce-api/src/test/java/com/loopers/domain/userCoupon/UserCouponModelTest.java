package com.loopers.domain.userCoupon;

import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserCouponModelTest {

    @DisplayName("사용자의 쿠폰을 사용할 때,")
    @Nested
    class ApplyCoupon{
        @DisplayName("사용자의 로그인아이디와 쿠폰의 로그인아이디가 같지 않은 경우, 에러를 반환한다.")
        @Test
        void returnErrorCode_whenUserLoginIdIsNotEqualsUserCouponLoginId(){
            // arrange
            UserModel user = new UserModel(
                    "login123",
                    "email@email.com",
                    "1999-01-01",
                    "W"
            );

            UserCouponModel userCouponModel = new UserCouponModel(
                    "usercoupon123",
                    "user123",
                    "coupon123",
                    ""
            );

            // act assert
            CoreException result = assertThrows(CoreException.class, () -> {
                userCouponModel.validateOwner(user);
            });

            assertEquals(ErrorType.NOT_FOUND, result.getErrorType());
        }

        @DisplayName("이미사용한 쿠폰이라면, 에러를 반환한다.")
        @Test
        void returnErrorCode_whenUserCouponIsUsed(){
            // arrange
            UserCouponModel userCouponModel = new UserCouponModel(
                    "usercoupon123",
                    "user123",
                    "coupon123",
                    "2029-03-05"
            );
            userCouponModel.use();

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                userCouponModel.use();
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

        }
    }
}
