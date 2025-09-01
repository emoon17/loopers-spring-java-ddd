package com.loopers.domain.coupon;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.userCoupon.UserCouponModel;
import com.loopers.domain.userCoupon.UserCouponService;
import com.loopers.infrastructure.coupon.CouponJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.infrastructure.userCoupon.UserCouponJapRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.loopers.domain.coupon.CouponModel.CouponType.FIXED;
import static com.loopers.domain.coupon.CouponModel.TargetType.PRODUCT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertThrows;

@SpringBootTest
public class CouponServiceTest {


    @Autowired
    private CouponService couponService;

    @Autowired
    private CouponJpaRepository couponJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private UserCouponJapRepository userCouponJapRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    public void tearDown() { databaseCleanUp.truncateAllTables(); }

    @DisplayName("쿠폰을 조회할 때,")
    @Nested
    class GetCoupon{
        @DisplayName("사용자의 쿠폰이 주문 쿠폰 대상 상품에 포함되지 않은 경우, 예외를 던진다.")
        @Test
        void returnErrorCode_whenCouponTargetProductDoesNotExistInOrderItems(){
            CouponModel coupon = new CouponModel(
                    "coupon-001",
                    CouponModel.CouponType.FIXED,
                    1000L,
                    null,
                    CouponModel.TargetType.PRODUCT,
                    "product-123",
                    "2025-01-01",
                    "2025-12-31"
            );


            List<OrderItemModel> orderItems = List.of(
                    new OrderItemModel("item1", "order1", "otherProduct", 1L, 10000L)
            );

            // act & assert
            CoreException exception = assertThrows(CoreException.class, () -> {
                orderItems.stream()
                        .filter(item -> coupon.getTargetId().equals(item.getProductId()))
                        .findAny()
                        .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "쿠폰을 적용할 수 있는 상품이 없습니다."));
            });

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

        }
    }

    @DisplayName("쿠폰이 존재하지 않는 쿠폰일 경우, 예외를 던진다")
    @Test
    void returnErrorCode_whenCouponIsNotExist(){

        //arrange
        String couponId = "coupon-001";

        // act
        CoreException exception = assertThrows(CoreException.class, () -> {
            couponService.getCouponbyCouponIdWithLock(couponId);
        });

        // assert
        assertThat(exception.getErrorType()).isEqualTo(ErrorType.NOT_FOUND);


    }


}


