package com.loopers.domain.order;

import com.loopers.domain.cart.CartItemModel;
import com.loopers.domain.cart.CartModel;
import com.loopers.domain.cart.CartService;
import com.loopers.domain.point.PointModel;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.user.UserModel;
import com.loopers.infrastructure.cart.CartItemJpaRepository;
import com.loopers.infrastructure.cart.CartJpaRepository;
import com.loopers.infrastructure.order.OrderItemJpaRepository;
import com.loopers.infrastructure.order.OrderJpaRepository;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.infrastructure.product.ProductJpaRepository;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

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

    @AfterEach
    void tearDown() { databaseCleanUp.truncateAllTables(); }

    @DisplayName("장바구니에서 주문할 때,")
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
    }

}
