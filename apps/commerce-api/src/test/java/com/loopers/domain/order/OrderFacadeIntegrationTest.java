package com.loopers.domain.order;

import com.loopers.application.order.OrderFacade;
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
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static com.loopers.domain.coupon.CouponModel.CouponType.FIXED;
import static com.loopers.domain.coupon.CouponModel.TargetType.PRODUCT;

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
    private CartItemJpaRepository  cartItemJpaRepository;

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

    @AfterEach
    void tearDown() { databaseCleanUp.truncateAllTables(); }

    @DisplayName("사용자가 주문할 때,")
    @Nested
    class CreateOrder {
        @DisplayName("포인트 차감, 재고 차감 후 주문 저장하여 주문정보를 반환한다.")
        @Test
        void returnOrderInfo_whenCartToOrder(){

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
            Assertions.assertEquals(1, orderJpaRepository.findAll().size());
            Assertions.assertEquals(1, orderItemJpaRepository.findAll().size());
            Assertions.assertEquals(10000L - price * quantity, pointService.getPointModelByLoginId(loginId).getAmount());
            Assertions.assertEquals(8L, productService.getProductByProductId(productId).get().getStock());

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
            Assertions.assertThrows( CoreException.class, () -> orderFacade.createOrderFromCart(user, usercoupon.getUserCouponId()));

            Assertions.assertEquals(0, orderJpaRepository.findAll().size());
            Assertions.assertEquals(10L, productService.getProductByProductId(productId).get().getStock());
            Assertions.assertEquals(100L, pointService.getPointModelByLoginId(loginId).getAmount());
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
            Assertions.assertThrows( CoreException.class, () -> orderFacade.createOrderFromCart(user, usercoupon.getUserCouponId()));

            Assertions.assertEquals(0, orderJpaRepository.findAll().size());
            Assertions.assertEquals(1L, productService.getProductByProductId(productId).get().getStock());
            Assertions.assertEquals(100000L, pointService.getPointModelByLoginId(loginId).getAmount());
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

            Assertions.assertEquals(0, orderJpaRepository.findAll().size());
            Assertions.assertEquals(5L, productService.getProductByProductId(productId).get().getStock());
            Assertions.assertEquals(5000L, pointService.getPointModelByLoginId(loginId).getAmount());
        }


        @DisplayName("동시성체크")
        @Nested
        class CoCurrencyOrderCheck {
            @DisplayName("동일한 상품에 대해 여러 주문이 동시에 요청되어도, 재고가 정상적으로 차감된다.")
            @Test
            void stockIsCorrectlyDecreased_whenMultipleOrderRequestSameProduct()throws InterruptedException{
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

                for(int i = 0; i < countDownLatchCount; i++){
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

                       } catch (Exception e){
                           System.out.println("에러::::::: " + e.getMessage());
                       }finally {
                           countDownLatch.countDown();
                       }

                    });
                }
                countDownLatch.await();

                ProductModel finalProduct = productService.getProductByProductId(productId).orElseThrow();
                Assertions.assertEquals(0L, finalProduct.getStock());

                Assertions.assertEquals(20, orderJpaRepository.findAll().size());
            }
        }
    }

}
