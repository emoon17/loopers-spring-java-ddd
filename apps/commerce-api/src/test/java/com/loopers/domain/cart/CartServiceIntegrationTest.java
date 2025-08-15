package com.loopers.domain.cart;

import com.loopers.application.cart.CartInfo;
import com.loopers.application.cart.CartItemInfo;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.user.UserModel;
import com.loopers.infrastructure.cart.CartItemJpaRepository;
import com.loopers.infrastructure.cart.CartJpaRepository;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class CartServiceIntegrationTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private CartItemJpaRepository cartItemJpaRepository;

    @Autowired
    private CartJpaRepository cartJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() { databaseCleanUp.truncateAllTables(); }

    @DisplayName("카트에 상품을 담을 때,")
    @Nested
    class AddCart {
        @DisplayName("카트가 없었을 경우 카트를 새로 만든 후, cartId를 반환한다.")
        @Test
        void returnCartId_whenCartIdIsNotExist() {
            // Arrange
            UserModel user = new UserModel(
                    "user123",
                    "test@email.com",
                    "1999-01-01",
                    "W"
            );

            // Act
            CartModel cart = cartService.getOrCreateCart(user);

            // Assert
            Optional<CartModel> savedCart = cartJpaRepository.findCartByLoginId("user123");
            assertEquals("user123", cart.getLoginId());
            assertEquals(savedCart.get().getCartId(), cart.getCartId());
        }

        @DisplayName("카트가 있었을 경우, 해당 유저의 cartId를 반환한다.")
        @Test
        void returnCartId_whenCartIdIsExist() {
            // Arrange
            UserModel user = new UserModel(
                    "user123",
                    "test@email.com",
                    "1999-01-01",
                    "W"
            );
            CartModel existingCart = CartModel.create(user.getLoginId());
            cartJpaRepository.save(existingCart);

            // Act
            CartModel cart = cartService.getOrCreateCart(user);

            // Assert
            assertEquals(existingCart.getCartId(), cart.getCartId());
        }

//        @DisplayName("카트가 있고, 해당 상품이 없는 경우 아이템을 새로 카트에 넣는다.")
//        @Test
//        void createCartItem_whenCartIsExistAndCartItemNotExist() {
//            // Arrange
//            UserModel user = new UserModel(
//                    "user123",
//                    "test@email.com",
//                    "1999-01-01",
//                    "W"
//            );
//            CartModel cart = CartModel.create(user.getLoginId());
//            cartJpaRepository.save(cart);
//
//            ProductModel product = new ProductModel("p001", "나이키", "신발쓰", "brand01", 1000L, 10L);
//            CartItemModel cartItem = new CartItemModel("item001", cart.getCartId(), product.getProductId(), 1L, 1000L);
//
//            // Act
//            cartService.addOrUpdateCartItem(cartItem, product);
//
//            // Assert
//            Optional<CartItemModel> savedItem = cartItemJpaRepository.findByCartIdAndProductId(cart.getCartId(), product.getProductId());
//            assertEquals(1, savedItem.get().getQuantity());
//            assertEquals(1000, savedItem.get().getPrice());
//
//        }

//        @DisplayName("카트가 있고, 해당 상품도 있는 경우 아이템의 수량과 가격을 추가한다.")
//        @Test
//        void addCartItem_whenCartIsExistAndCartItemIsExist(){
//            // Arrange
//            UserModel user = new UserModel(
//                    "user123",
//                    "test@email.com",
//                    "1999-01-01",
//                    "W"
//            );
//
//            CartModel cart = CartModel.create(user.getLoginId());
//            cartJpaRepository.save(cart);
//
//            ProductModel product = new ProductModel("p001", "상품", "설명", "brand01", 1000L, 10L);
//            CartItemModel existingItem = new CartItemModel("item001", cart.getCartId(), product.getProductId(), 1L, 1000L);
//            cartItemJpaRepository.save(existingItem);
//
//            CartItemModel inputItem = new CartItemModel("anyId", cart.getCartId(), product.getProductId(), 2L, 2000L);
//
//            // Act
//            cartService.addOrUpdateCartItem(inputItem, product);
//
//            // Assert
//            Optional<CartItemModel> updatedItem = cartItemJpaRepository.findByCartIdAndProductId(cart.getCartId(), product.getProductId());
//            assertEquals(3, updatedItem.get().getQuantity()); // 1 + 2
//            assertEquals(3000, updatedItem.get().getPrice()); // 1000 * 3
//
//        }

        @DisplayName("유저의 장바구니 정보를 반환한다.")
        @Test
        void returnCartInfo_whenUserHasCartItems() {
            // Arrange
            UserModel user = new UserModel(
                    "user123",
                    "test@email.com",
                    "1995-01-01",
                    "W"
            );

            CartModel cart = CartModel.create(user.getLoginId());
            cartJpaRepository.save(cart);

            CartItemModel item1 = new CartItemModel(
                    "item001",
                    cart.getCartId(),
                    "product01",
                    2L,
                    2000L
            );
            CartItemModel item2 = new CartItemModel(
                    "item002",
                    cart.getCartId(),
                    "product02",
                    1L,
                    1000L
            );
            cartItemJpaRepository.save(item1);
            cartItemJpaRepository.save(item2);

            cart.updateSummary(3, 3000);
            cartJpaRepository.save(cart);

            // Act
            CartModel foundCart = cartService.getOrCreateCart(user);
            List<CartItemModel> foundItems = cartService.getAllCart(foundCart);

            List<CartItemInfo> itemInfos = foundItems.stream()
                    .map(CartItemInfo::from)
                    .toList();

            CartInfo cartInfo = CartInfo.from(foundCart, itemInfos);

            // Assert
            assertEquals(cart.getCartId(), cartInfo.cartId());
            assertEquals(3, cartInfo.totalQuantity());
            assertEquals(3000, cartInfo.totalPrice());
            assertEquals(2, cartInfo.items().size());
            assertEquals("product01", cartInfo.items().get(0).productId());
        }


    }
}
