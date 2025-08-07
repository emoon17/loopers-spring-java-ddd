package com.loopers.domain.cart;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.user.UserModel;

import java.util.List;
import java.util.Optional;

public interface CartRepository {

    Optional<CartModel> findCartByLoginId(UserModel user);
    Optional<CartModel> findCartByLoginIdWithLock(UserModel user);
    Optional<CartModel> findCartByCartId(String cartId);
    void saveCart(CartModel cart);
    void clearCart(CartModel cart);
    Optional<CartItemModel> findCartItemByCartIdProductId(CartItemModel cartItem, ProductModel product);
    List<CartItemModel> findCartItemsByCartIdWithLock(String cartId);
    void saveCartItem(CartItemModel cartItem);
}
