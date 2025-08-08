package com.loopers.infrastructure.cart;

import com.loopers.domain.cart.CartItemModel;
import com.loopers.domain.cart.CartModel;
import com.loopers.domain.cart.CartRepository;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CartRepositoryImpl implements CartRepository {
    private final CartJpaRepository cartJpaRepository;
    private final CartItemJpaRepository cartItemJpaRepository;

    @Override
    public Optional<CartModel> findCartByLoginId(UserModel user) {
        return cartJpaRepository.findCartByLoginId(user.getLoginId());
    }

    @Override
    public Optional<CartModel> findCartByLoginIdWithLock(UserModel user) {
        return cartJpaRepository.findCartByLoginIdWithLock(user.getLoginId());
    }

    @Override
    public Optional<CartModel> findCartByCartId(String cartId) {
        return cartJpaRepository.findCartByCartId(cartId);
    }


    @Override
    public CartModel saveCart(CartModel cart) {
        return cartJpaRepository.save(cart);
    }

    @Override
    public void clearCart(CartModel cart) {
        cartJpaRepository.delete(cart);
    }

    @Override
    public Optional<CartItemModel> findCartItemByCartIdProductId(CartItemModel cartItem, ProductModel product) {
        return cartItemJpaRepository
                .findByCartIdAndProductId(cartItem.getCartId(), product.getProductId());
    }


    @Override
    public void saveCartItem(CartItemModel cartItem) {
        cartItemJpaRepository.save(cartItem);
    }

    @Override
    public List<CartItemModel> findCartItemsByCartIdWithLock(String cartId) {
        return cartItemJpaRepository.findCartItemsByCartIdWithLock(cartId);
    }

}
