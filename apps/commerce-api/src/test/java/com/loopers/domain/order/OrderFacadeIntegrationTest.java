package com.loopers.domain.order;

import com.loopers.application.coupon.CouponFacade;
import com.loopers.application.order.OrderFacade;
import com.loopers.application.order.OrderInfo;
import com.loopers.domain.cart.CartItemModel;
import com.loopers.domain.cart.CartModel;
import com.loopers.domain.cart.CartService;
import com.loopers.domain.coupon.CouponModel;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserModel;
import com.loopers.domain.userCoupon.UserCouponModel;
import com.loopers.domain.userCoupon.UserCouponService;
import com.loopers.infrastructure.cart.CartItemJpaRepository;
import com.loopers.infrastructure.cart.CartJpaRepository;
import com.loopers.infrastructure.coupon.CouponJpaRepository;
import com.loopers.infrastructure.order.OrderItemJpaRepository;
import com.loopers.infrastructure.order.OrderJpaRepository;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.infrastructure.userCoupon.UserCouponJapRepository;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import jakarta.persistence.OptimisticLockException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static com.loopers.domain.coupon.CouponModel.CouponType.FIXED;
import static com.loopers.domain.coupon.CouponModel.TargetType.PRODUCT;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
    private CouponFacade couponFacade;

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

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    @DisplayName("사용자가 주문할 때,")
    @Nested
    class CreateOrder {
        @DisplayName("포인트 차감, 재고 차감 후 주문 저장하여 주문정보를 반환한다.")
        @Test
        void returnOrderInfo_whenCartToOrder() {

            //Assert
            String loginId = "user123";
            String productId = "product-001";
            long quantity = 2;
            long price = 1000L;

            // 상품 저장
            ProductModel product = new ProductModel(productId, "나이키", "신발", "b001", price, 10L);
            productService.saveProduct(product);

            // 포인트 저장
            PointModel point = new PointModel(loginId, 10000L);
            pointJpaRepository.save(point);

            // 유저, 장바구니 생성
            UserModel user = new UserModel(loginId, "test@email.com", "1999-01-01", "W");
            userJpaRepository.save(user);
            CartModel cart = cartService.getOrCreateCart(user);
            cartJpaRepository.save(cart);

            // 장바구니 아이템 추가
            CartItemModel cartItem = new CartItemModel(UUID.randomUUID().toString(), cart.getCartId(), productId, quantity, price);
            cartItemJpaRepository.saveAll(List.of(cartItem));

            // Act
            List<CartItemModel> cartItems = cartService.getAllCart(cart);

            List<OrderItemModel> orderItems = cartItems.stream()
                    .map(item -> OrderItemModel.from(item, null))
                    .toList();

            OrderModel order = OrderModel.create(loginId, orderItems, "CART");
            orderItems.forEach(item -> item.assignOrderId(order.getOrderId()));

            pointService.usePoint(loginId, order.getTotalPrice());

            for (OrderItemModel item : orderItems) {
                ProductModel target = productService.getProductByProductId(item.getProductId()).orElseThrow();
                target.decreaseStock(item.getQuantity());
                productService.saveProduct(target);
            }

            orderService.saveOrder(order);
            orderService.saveOrderItems(orderItems);


            // Assert
            assertEquals(1, orderJpaRepository.findAll().size());
            assertEquals(1, orderItemJpaRepository.findAll().size());
            assertEquals(10000L - price * quantity, pointService.getPointModelByLoginId(loginId).getAmount());
            assertEquals(8L, productService.getProductByProductId(productId).get().getStock());

        }

        @DisplayName("포인트가 부족할 경우 주문은 실패하고, 전체 롤백처리가 되어야한다.")
        @Test
        void rollback_whenPointNotEnough() {
            // Arrange
            String loginId = "user123";
            String productId = "p001";
            long quantity = 2;
            long price = 1000L;

            ProductModel product = new ProductModel(productId, "나이키", "신발", "b001", price, 10L);
            productService.saveProduct(product);

            UserModel user = new UserModel(loginId, "email@test.com", "1999-01-01", "M");
            userJpaRepository.save(user);
            CartModel cart = cartService.getOrCreateCart(user);
            cartItemJpaRepository.save(new CartItemModel(UUID.randomUUID().toString(), cart.getCartId(), productId, quantity, price));

            pointService.savePoint(new PointModel(loginId, 100L));

            UserCouponModel usercoupon = new UserCouponModel(
                    "usercoupon1",
                    "user123",
                    "coupon123",
                    "2025-01-01"
            );

            // Act & Assert
            Assertions.assertThrows(CoreException.class, () -> orderFacade.createOrderFromCart(user, usercoupon.getUserCouponId()));

            assertEquals(0, orderJpaRepository.findAll().size());
            assertEquals(10L, productService.getProductByProductId(productId).get().getStock());
            assertEquals(100L, pointService.getPointModelByLoginId(loginId).getAmount());
        }

        @DisplayName("재고가 부족한 경우 주문은 실패하고, 전체 롤백된다.")
        @Test
        void rollback_whenStockNotEnough() {
            // Arrange
            String loginId = "user123";
            String productId = "p001";
            long quantity = 5;
            long price = 1000L;

            ProductModel prduct = new ProductModel(productId, "나이키", "신발", "b001", price, 1L);
            productService.saveProduct(prduct);

            pointService.savePoint(new PointModel(loginId, 100000L));

            UserModel user = new UserModel(
                    loginId,
                    "email@test.com",
                    "1999-01-01",
                    "W"
            );
            userJpaRepository.save(user);

            UserCouponModel usercoupon = new UserCouponModel(
                    "usercoupon1",
                    "user123",
                    "coupon123",
                    "2025-01-01"
            );

            CartModel cart = cartService.getOrCreateCart(user);
            cartItemJpaRepository.save(new CartItemModel(UUID.randomUUID().toString(), cart.getCartId(), productId, quantity, price));

            // Act Assert
            Assertions.assertThrows(CoreException.class, () -> orderFacade.createOrderFromCart(user, usercoupon.getUserCouponId()));

            assertEquals(0, orderJpaRepository.findAll().size());
            assertEquals(1L, productService.getProductByProductId(productId).get().getStock());
            assertEquals(100000L, pointService.getPointModelByLoginId(loginId).getAmount());
        }

        @DisplayName("쿠폰에서 작업이 실패할 경우 롤백처리한다.")
        @Test
        void returnRollback_whenCouponFailed() {
            // arrange
            String loginId = "user123";
            String productId = "p001";
            long quantity = 1;
            long price = 1000L;

            ProductModel product = new ProductModel(productId, "나이키", "신발", "b001", price, 5L);
            productService.saveProduct(product);

            UserModel user = new UserModel(loginId, "email@test.com", "1999-01-01", "M");
            userJpaRepository.save(user);

            pointService.savePoint(new PointModel(loginId, 5000L));

            CartModel cart = cartService.getOrCreateCart(user);
            cartItemJpaRepository.save(new CartItemModel(
                    UUID.randomUUID().toString(), cart.getCartId(), productId, quantity, price
            ));

            UserCouponModel invalidUserCoupon = new UserCouponModel(
                    "usercoupon1",
                    loginId,
                    "non-exist-coupon-id",
                    "2025-01-01"
            );

            // act & assert
            Assertions.assertThrows(CoreException.class, () -> {
                orderFacade.createOrderFromCart(user, invalidUserCoupon.getCouponId());
            });

            assertEquals(0, orderJpaRepository.findAll().size());
            assertEquals(5L, productService.getProductByProductId(productId).get().getStock());
            assertEquals(5000L, pointService.getPointModelByLoginId(loginId).getAmount());
        }


        @DisplayName("동시성체크")
        @Nested
        class CoCurrencyOrderCheck {
            @DisplayName("동일한 상품에 대해 여러 주문이 동시에 요청되어도, 재고가 정상적으로 차감된다.")
            @Test
            void stockIsCorrectlyDecreased_whenMultipleOrderRequestSameProduct() throws InterruptedException {
                // arrange
                int threadCount = 30;
                int countDownLatchCount = 40;
                ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
                CountDownLatch countDownLatch = new CountDownLatch(countDownLatchCount);

                String productId = "p001";
                long price = 1000L;

                long stock = 100L;
                long orderStock = 5L;

                productService.saveProduct(new ProductModel(productId, "나이키", "신발", "b001", price, stock));
                couponJpaRepository.save(
                        new CouponModel(
                                "counpon123",
                                FIXED,
                                5000L,
                                null,
                                PRODUCT,
                                productId,
                                "2025-01-01",
                                "2025-12-31"
                        )
                );

                for (int i = 0; i < countDownLatchCount; i++) {
                    final int idx = i;
                    executorService.submit(() -> {
                        try {
                            String loginId = "user123" + idx;
                            UserModel user = new UserModel(loginId, loginId + "@test.com", "1999-01-01", "M");
                            userJpaRepository.save(user);

                            pointService.savePoint(new PointModel(loginId, 100000L));

                            CartModel cart = cartService.getOrCreateCart(user);
                            cartItemJpaRepository.save(new CartItemModel(UUID.randomUUID().toString(), cart.getCartId(), productId, orderStock, price));

                            UserCouponModel usercoupon = new UserCouponModel(
                                    "userCoupon" + idx,
                                    loginId,
                                    "counpon123",
                                    "2025-01-01"
                            );
                            userCouponJapRepository.save(usercoupon);
                            userCouponJapRepository.flush();

                            orderFacade.createOrderFromCart(user, "userCoupon" + idx);

                        } catch (Exception e) {
                            System.out.println("에러::::::: " + e.getMessage());
                        } finally {
                            countDownLatch.countDown();
                        }

                    });
                }
                countDownLatch.await();

                ProductModel finalProduct = productService.getProductByProductId(productId).orElseThrow();
                assertEquals(0L, finalProduct.getStock());

                assertEquals(20, orderJpaRepository.findAll().size());
            }

            @DisplayName("동일한 유저가 서로 다른 주문을 동시에 수행해도, 포인트가 정상적으로 차감된다.")
            @Test
            void pointIsCorrectlyDeducted_whenSameUserPlacesDifferentOrdersConcurrently() throws InterruptedException {
                // arrange
                // arrange
                String loginId = "user123";
                String productId = "P001";
                String couponId = "coupon001";
                String userCouponId = "userCoupon001";
                long price = 1000L;

                // 유저 생성 & 저장
                UserModel user = userJpaRepository.save(
                        new UserModel(loginId, loginId + "@t.com", "1999-01-01", "M")
                );

                // 상품 생성 & 저장
                productService.saveProduct(
                        new ProductModel(productId, "상품", "설명", "B001", price, 1000L)
                );

                // 포인트 생성 & 저장
                pointService.savePoint(new PointModel(loginId, 1_000_000L));

                // 쿠폰 생성 & 저장
                couponJpaRepository.save(
                        new CouponModel(couponId, FIXED, 5000L, null, PRODUCT, productId, "2025-01-01", "2025-12-31")
                );

                // 유저 쿠폰 발급 (모든 스레드가 같은 쿠폰 사용)
                userCouponJapRepository.save(
                        new UserCouponModel(userCouponId, loginId, couponId, "2025-01-01")
                );

                // 장바구니 미리 생성 (Deadlock 방지)
                CartModel cart = cartService.getOrCreateCart(user);

                int threadCount = 10;
                ExecutorService executor = Executors.newFixedThreadPool(threadCount);
                CountDownLatch startLatch = new CountDownLatch(1);
                CountDownLatch doneLatch = new CountDownLatch(threadCount);

                AtomicInteger successCount = new AtomicInteger();
                AtomicInteger alreadyUsedCount = new AtomicInteger();
                AtomicInteger deadlockCount = new AtomicInteger();

                for (int i = 0; i < threadCount; i++) {
                    executor.submit(() -> {
                        try {
                            // 장바구니 아이템만 추가 (cart 생성 안 함)
                            cartItemJpaRepository.save(new CartItemModel(
                                    UUID.randomUUID().toString(),
                                    cart.getCartId(),
                                    productId,
                                    1L,
                                    price
                            ));

                            startLatch.await(); // 동시에 시작
                            orderFacade.createOrderFromCart(user, userCouponId);
                            successCount.incrementAndGet();
                        } catch (CoreException e) {
                            if (e.getMessage().contains("이미 사용된 쿠폰")) {
                                alreadyUsedCount.incrementAndGet();
                            } else {
                                System.out.println("다른 CoreException 발생: " + e.getMessage());
                            }
                        } catch (CannotAcquireLockException e) {
                            // Deadlock 방지용 카운트
                            deadlockCount.incrementAndGet();
                        } catch (Exception e) {
                            e.printStackTrace();
                        } finally {
                            doneLatch.countDown();
                        }
                    });
                }

                // 모든 스레드 시작
                startLatch.countDown();
                doneLatch.await();

                // assert
                assertEquals(1, successCount.get(), "쿠폰이 적용된 주문은 1건이어야 한다");
                assertEquals(threadCount - 1, alreadyUsedCount.get(), "나머지는 쿠폰 이미 사용 예외여야 한다");

                // 쿠폰은 최종적으로 사용 상태여야 함
                UserCouponModel usedCoupon = userCouponJapRepository.findById(userCouponId).orElseThrow();
                assertTrue(usedCoupon.isUsed());

                System.out.println("성공: " + successCount.get());
                System.out.println("이미 사용 예외: " + alreadyUsedCount.get());
                System.out.println("Deadlock 예외: " + deadlockCount.get());
            }

        }
        @DisplayName("동일한 쿠폰으로 여러 기기에서 동시에 주문해도, 쿠폰은 단 한번만 사용된다.")
        @Test
        void onlyOneCouponUsage_shouldBeAllowed_whenMultipleThreadsAttemptToUseSameCoupon() throws InterruptedException {
            // arrange
            String loginId = "user123";
            String productId = "P001";
            String couponId = "coupon001";
            String userCouponId = "userCoupon001";
            long price = 1000L;

            // 유저 생성 & 저장
            UserModel user = userJpaRepository.save(
                    new UserModel(loginId, loginId + "@t.com", "1999-01-01", "M")
            );

            // 상품 생성 & 저장
            productService.saveProduct(
                    new ProductModel(productId, "운동화", "런닝화", "B001", price, 1000L)
            );

            // 포인트 생성 & 저장
            pointService.savePoint(new PointModel(loginId, 1_000_000L));

            // 쿠폰 생성 & 저장
            couponJpaRepository.save(
                    new CouponModel(couponId, FIXED, 5000L, null, PRODUCT, productId, "2025-01-01", "2025-12-31")
            );

            // 유저 쿠폰 발급
            userCouponJapRepository.save(
                    new UserCouponModel(userCouponId, loginId, couponId, "2025-01-01")
            );

            int threadCount = 10;
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger couponAppliedCount = new AtomicInteger();
            AtomicInteger couponFailCount = new AtomicInteger();

            CartModel cart =  new CartModel(
                    "cart123",
                    loginId,
                    0,
                    0
            );
            cartJpaRepository.save(cart);


            for (int i = 0; i < threadCount; i++) {
                int idx = i;
                executor.submit(() -> {
                    try {
                        cartItemJpaRepository.save(
                                new CartItemModel(UUID.randomUUID().toString(), cart.getCartId(), productId, 1L, price)
                        );
                        cartItemJpaRepository.flush();

                        startLatch.await();
                        orderFacade.createOrderFromCart(user, userCouponId);
                        couponAppliedCount.incrementAndGet();
                    } catch (CoreException e) {
                        if (e.getMessage().contains("이미 사용된 쿠폰")) {
                            couponFailCount.incrementAndGet();
                        } else {
                            System.out.println("다른 CoreException: " + e.getMessage());
                        }
                    } catch (ObjectOptimisticLockingFailureException | OptimisticLockException e) {
                        couponFailCount.incrementAndGet();
                    } catch (Exception e) {
                        System.out.println("error :::: " + idx + "ddd " + e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            // 모든 스레드 시작
            startLatch.countDown();
            doneLatch.await();

            // assert
            assertEquals(1, couponAppliedCount.get(), "쿠폰이 적용된 주문은 1건이어야 한다");
            assertEquals(threadCount - 1, couponFailCount.get(), "나머지는 쿠폰 이미 사용 예외여야 한다");

            UserCouponModel usedCoupon = userCouponJapRepository.findById(userCouponId).orElseThrow();
            assertTrue(usedCoupon.isUsed(), "쿠폰은 사용 상태여야 한다");
        }


    }


}
