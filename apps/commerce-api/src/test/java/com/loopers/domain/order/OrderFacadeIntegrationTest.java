package com.loopers.domain.order;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.brand.BrandModel;
import com.loopers.domain.cart.CartItemModel;
import com.loopers.domain.cart.CartModel;
import com.loopers.domain.cart.CartService;
import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.payments.PaymentStatus;
import com.loopers.domain.payments.PaymentsModel;
import com.loopers.domain.payments.PaymentsService;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.userCoupon.UserCouponModel;
import com.loopers.domain.userCoupon.UserCouponService;
import com.loopers.infrastructure.brand.BrandJpaRepository;
import com.loopers.infrastructure.cart.CartItemJpaRepository;
import com.loopers.infrastructure.cart.CartJpaRepository;
import com.loopers.infrastructure.coupon.CouponJpaRepository;
import com.loopers.infrastructure.order.OrderItemJpaRepository;
import com.loopers.infrastructure.order.OrderJpaRepository;
import com.loopers.infrastructure.payments.PaymentsJpaRepository;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.infrastructure.userCoupon.UserCouponJapRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.context.transaction.TestTransaction;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.loopers.domain.coupon.CouponModel.CouponType.FIXED;
import static com.loopers.domain.coupon.CouponModel.TargetType.PRODUCT;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@SpringBootTest
public class OrderFacadeIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private PointService pointService;

    @Autowired
    private OrderFacade orderFacade;

    @Autowired
    private PointJpaRepository pointJpaRepository;

    @Autowired
    private CartJpaRepository cartJpaRepository;

    @Autowired
    private CartItemJpaRepository cartItemJpaRepository;

    @Autowired
    private OrderJpaRepository orderJpaRepository;

    @Autowired
    private OrderItemJpaRepository orderItemJpaRepository;

    @Autowired
    private ProductJpaRepository productJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;
    @Autowired
    private UserJpaRepository userJpaRepository;
    @Autowired
    private CouponJpaRepository couponJpaRepository;
    @Autowired
    private UserCouponJapRepository userCouponJapRepository;
    @Autowired
    private UserCouponService userCouponService;
    @Autowired
    private PaymentsService paymentsFacade;
    @Autowired
    private PaymentsJpaRepository paymentsJpaRepository;
    @Autowired
    private BrandJpaRepository brandJpaRepository;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("사용자가 주문할 때,")
    @Nested
    class CreateOrder {
        @Test
        void checkoug_호출시_주문저장되고_커밋후_pg요청된다(){
            // arrange
            var user = new UserModel("login123", "email@test.com", "1999-01-01", "M");
            var brand = new BrandModel("brand001", "테스트브랜드");
            userJpaRepository.save(user);
            brandJpaRepository.save(brand);
            ZonedDateTime now = ZonedDateTime.now();

            productJpaRepository.save(new ProductModel(
                    "P-1", "테스트상품", "test", brand,5000L, 10L, now, now
            ));

            CartModel cart = cartService.getOrCreateCart(user);
            cartJpaRepository.save(cart);

            CartItemModel cartItem = new CartItemModel(UUID.randomUUID().toString(), cart.getCartId(), "P-1", 2L, 5000L);
            cartItemJpaRepository.saveAll(List.of(cartItem));

            String callbackUrl = "http://localhost:8080/api/v1/payments/callback";

            // act
            var orderInfo = orderFacade.createOrderFromCart(
                    user, null,  100L,"SAMSUNG", "1234-5678-9814-1451", callbackUrl
            );

            // assert
            // 1. 주문이 결제대기 상태로 저장되었는지
            OrderModel order = orderService.findOrderById(orderInfo.orderId());
            assertThat(order.getStatus()).isEqualTo(OrderStatus.WAITING_FOR_PAYMENT);
            assertThat(order.getTotalPrice()).isGreaterThan(0);

            // 2. 결제시도(PENDING)가 생성되었는지
            PaymentsModel latest = paymentsJpaRepository.findByOrderIdLatest(order.getOrderId());
            assertThat(latest).isNotNull();
            assertThat(latest.getStatus().name()).isEqualTo("PENDING");

            // 3. 트랜잭션 커밋 후 PG 호출한 뒤 transactionId가 붙는지
            var saved = paymentsJpaRepository.findByOrderIdLatest(order.getOrderId());
            if(saved.getStatus() == PaymentStatus.PENDING){
                assertThat(saved.getTransactionId()).isNull();
            } else{
                assertThat(saved.getTransactionId()).isNotBlank();
            }
        }


    }


}
