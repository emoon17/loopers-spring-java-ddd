package com.loopers.application;

import com.loopers.application.order.OrderFacade;
import com.loopers.application.payment.PaymentsFacade;
import com.loopers.application.payment.PaymentsScheduler;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderStatus;
import com.loopers.domain.payments.PaymentStatus;
import com.loopers.domain.payments.PaymentsModel;
import com.loopers.domain.payments.port.PgClientPort;
import com.loopers.domain.product.ProductModel;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.order.OrderItemJpaRepository;
import com.loopers.infrastructure.order.OrderJpaRepository;
import com.loopers.infrastructure.payments.PaymentsJpaRepository;
import com.loopers.infrastructure.pg.PgClientAdapter;
import com.loopers.infrastructure.product.ProductJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Order;
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
    private PaymentsFacade paymentsFacade;
    @MockitoBean
    PgClientAdapter pgClientAdapter;
    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @DisplayName("결제 상태 확인을 위한 스케줄러를 사용할 때,")
    @Nested
    class GetScheduler{
        @DisplayName("스케줄러가 PENDING 결제건을 PG 동기화 후 SUCCESS로 만들고 주문을 PAID 처리한다")
        @Test
        void PENDING_결게전_PG동기화후_SUCCESS로_전환하고_주문을_SUCCESS처리한다(){

            // arrange
            ZonedDateTime now = ZonedDateTime.now();
            BrandModel brand = new BrandModel("brand01", "brand");
            brandJpaRepository.save(brand);
            ProductModel product1 = new ProductModel(
                    "prd01",
                    "테스트",
                    "테스트",
                    brand,
                    10L,
                    1000L,
                    now,
                    now
            );
            productJpaRepository.save(product1);
            ProductModel product2 = new ProductModel(
                    "prd02",
                    "테스트",
                    "테스트",
                    brand,
                    20L,
                    2000L,
                    now,
                    now
            );
            productJpaRepository.save(product2);

            orderItemJpaRepository.save(
                    new OrderItemModel(
                            "orderItemId001",
                            "order01",
                            "prd01",
                            10L,
                            1000L
                    )
            );
            orderItemJpaRepository.save(
                    new OrderItemModel(
                            "orderItemId002",
                            "order01",
                            "prd02",
                            20L,
                            2000L
                    )
            );

            orderJpaRepository.save(
                    new OrderModel(
                            "order01",
                            "loginId01",
                            OrderStatus.WAITING_FOR_PAYMENT,
                            "CART",
                            null,
                            30L,
                            3000L,
                            "2025-01-01",
                            "2025-01-01"
                    )
            );

            PaymentsModel pending = PaymentsModel.newPending("loginId01", "order01", 3000L);
            paymentsJpaRepository.save(pending);


            when(pgClientAdapter.getTransactionIds(eq("loginId01"), anyString()))
                    .thenReturn(new PgClientPort.PgPaymentsResponse("tx-123", "order01", PaymentStatus.SUCCESS));

            // act
            paymentsScheduler.sync();

            // assert
            verify(orderFacade, times(1)).finalizeOrderAfterPayment("order01", PaymentStatus.SUCCESS);
            PaymentsModel updated = paymentsJpaRepository.findByOrderIdLatest("order01");
            System.out.println("결제 상태 = " + updated.getStatus());
            assertThat(updated.getStatus()).isEqualTo(PaymentStatus.SUCCESS);

            OrderModel updatedOrder = orderJpaRepository.findById("order01").orElseThrow();
            System.out.println("주문 상태 = " + updatedOrder.getStatus());
            assertThat(updatedOrder.getStatus()).isEqualTo(OrderStatus.PAID);

        }
    }


}
