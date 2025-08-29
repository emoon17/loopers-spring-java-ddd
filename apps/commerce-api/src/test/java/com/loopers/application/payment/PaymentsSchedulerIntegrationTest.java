package com.loopers.application.payment;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payments.PaymentStatus;
import com.loopers.domain.payments.PaymentsModel;
import com.loopers.domain.payments.PaymentsService;
import com.loopers.domain.payments.port.PgClientPort;
import com.loopers.domain.point.PointHistoryModel;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.userCoupon.UserCouponModel;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.coupon.CouponJpaRepository;
import com.loopers.infrastructure.order.OrderItemJpaRepository;
import com.loopers.infrastructure.order.OrderJpaRepository;
import com.loopers.infrastructure.payments.PaymentsJpaRepository;
import com.loopers.infrastructure.pg.PgClientAdapter;
import com.loopers.infrastructure.point.PointHistoryJpaRepository;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.userCoupon.UserCouponJapRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.time.ZonedDateTime;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
public class PaymentsSchedulerIntegrationTest {

    @Autowired
    PaymentsScheduler paymentsScheduler;
    @Autowired
    PaymentsJpaRepository paymentsJpaRepository;
    @Autowired
    OrderItemJpaRepository orderItemJpaRepository;
    @Autowired
    OrderJpaRepository orderJpaRepository;
    @Autowired
    ProductJpaRepository productJpaRepository;
    @MockitoSpyBean
    OrderFacade orderFacade;
    @MockitoSpyBean
    private PaymentsService paymentsService;
    @MockitoBean
    PgClientAdapter pgClientAdapter;
    @Autowired
    private BrandJpaRepository brandJpaRepository;
    @Autowired
    private CouponJpaRepository couponJpaRepository;
    @Autowired
    private UserCouponJapRepository userCouponJapRepository;
    @Autowired
    private PointJpaRepository pointJpaRepository;
    @Autowired
    private PointHistoryJpaRepository pointHistoryJpaRepository;

    @BeforeEach
    void setUp() {
        ZonedDateTime now = ZonedDateTime.now();
        BrandModel brand = new BrandModel("brand01", "brand");
        brandJpaRepository.save(brand);

        ProductModel product1 = new ProductModel("prd01","테스트1","설명1",brand,1000L,10L,now,now);
        productJpaRepository.save(product1);
        ProductModel product2 = new ProductModel("prd02","테스트2","설명2",brand,2000L,20L,now,now);
        productJpaRepository.save(product2);

        orderItemJpaRepository.save(new OrderItemModel("orderItemId001","order01","prd01",10L,1000L));
        orderItemJpaRepository.save(new OrderItemModel("orderItemId002","order01","prd02",20L,2000L));

        OrderModel order = orderJpaRepository.save(new OrderModel(
                "order01","loginId01",OrderStatus.WAITING_FOR_PAYMENT,
                "CART","userCouponId01",30L,3000L,"2025-01-01","2025-01-01"
        ));

        CouponModel coupon = new CouponModel(
                "coupon-001",
                CouponModel.CouponType.FIXED,
                1000L,        // discountAmount
                null,         // discountRate
                CouponModel.TargetType.PRODUCT,
                "prd01",
                "2025-01-01",
                "2025-12-31"
        );
        couponJpaRepository.save(coupon);

        UserCouponModel userCoupon = new UserCouponModel(
                "userCouponId01", "loginId01", "coupon-001", "2025-01-01"
        );
        userCoupon.use();
        userCouponJapRepository.save(userCoupon);

        // 포인트도 차감 처리
        PointModel point = new PointModel("loginId01", 5000L);
        point.usePoint(2000L);
        pointJpaRepository.save(point);
        pointHistoryJpaRepository.save(PointHistoryModel.used(order.getOrderId(), order.getLoginId(), 2000L));


        PaymentsModel pending = PaymentsModel.newPending("loginId01", "order01", 3000L);
        paymentsJpaRepository.save(pending);
    }

    @DisplayName("결제 상태 확인을 위한 스케줄러를 사용할 때,")
    @Nested
    class GetScheduler{
        @DisplayName("스케줄러가 PENDING 결제건을 PG 동기화 후 SUCCESS로 만들고 주문을 PAID 처리한다")
        @Test
        void PENDING_결게전_PG동기화후_SUCCESS로_전환하고_주문을_SUCCESS처리한다(){

            // arrange
            PaymentsModel pending = PaymentsModel.newPending("loginId01", "order01", 3000L);
            paymentsJpaRepository.save(pending);
            when(pgClientAdapter.getTransactionIds(eq("loginId01"), anyString()))
                    .thenReturn(new PgClientPort.PgPaymentsResponse("tx-123", "order01", PaymentStatus.SUCCESS));

            // act
            paymentsScheduler.sync();

            // assert
            verify(orderFacade, times(1))
                    .finalizeOrderAfterPayment(any(PaymentsModel.class), eq(PaymentStatus.SUCCESS));
            PaymentsModel updated = paymentsJpaRepository.findByOrderIdLatest("order01");
            System.out.println("결제 상태 = " + updated.getStatus());
            assertThat(updated.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

            OrderModel updatedOrder = orderJpaRepository.findById("order01").orElseThrow();
            System.out.println("주문 상태 = " + updatedOrder.getStatus());
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        }

        @Test
        @DisplayName("실패 시 → PG 동기화 후 FAILED로 전환, 주문 PAYMENT_FAILED 처리 + 보상 로직 실행")
        void 결제실패시_보상로직실행(){
            // arrange
            when(pgClientAdapter.getTransactionIds(eq("loginId01"), anyString()))
                    .thenReturn(new PgClientPort.PgPaymentsResponse("tx-999","order01",PaymentStatus.FAILED));

            // act
            paymentsScheduler.sync();

            // assert
            verify(orderFacade, times(1))
                    .finalizeOrderAfterPayment(any(PaymentsModel.class), eq(PaymentStatus.FAILED));

            PaymentsModel updatedPayment = paymentsJpaRepository.findByOrderIdLatest("order01");
            assertThat(updatedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);

            OrderModel updatedOrder = orderJpaRepository.findById("order01").orElseThrow();
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAYMENT_FAILED);

            // 보상 로직 확인
            ProductModel restoredProduct1 = productJpaRepository.findById("prd01").orElseThrow();
            assertThat(restoredProduct1.getStock()).isEqualTo(10L); // 초기 세팅값으로 복구

            ProductModel restoredProduct2 = productJpaRepository.findById("prd02").orElseThrow();
            assertThat(restoredProduct2.getStock()).isEqualTo(20L); // 초기 세팅값으로 복구

            PointModel restoredPoint = pointJpaRepository.findPointByLoginId("loginId01").orElseThrow();
            assertThat(restoredPoint.getAmount()).isEqualTo(5000L); // 초기 포인트로 복구

            UserCouponModel restoredUserCoupon = userCouponJapRepository.findUserCouponByUserCouponId("userCouponId01")
                    .orElseThrow();
            assertThat(restoredUserCoupon.isUsed()).isFalse(); // 사용 취소돼야 함
        }
    }


}
