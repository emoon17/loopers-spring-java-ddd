package com.loopers.domain.cart;

import com.loopers.domain.product.ProductModel;
import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;

    public CartModel getOrCreateCart(UserModel user) {
        return cartRepository.findCartByLoginId(user)
                .orElseGet(() -> {
                    CartModel newCart = CartModel.create(user.getLoginId());
                    cartRepository.saveCart(newCart);
                    return newCart;
                });
    }

    public void addOrUpdateCartItem(CartItemModel cartItem, ProductModel product) {
        Optional<CartItemModel> existingCartItem = cartRepository.findCartItemByCartIdProductId(cartItem, product);

        if(existingCartItem.isPresent()) {
            CartItemModel cartItemModel = existingCartItem.get();
            cartItemModel.increaseQuantity(Math.toIntExact(cartItem.getQuantity()));
            cartRepository.saveCartItem(cartItemModel);
        } else {
            cartRepository.saveCartItem(cartItem);
        }

        CartModel cart = cartRepository.findCartByCartId(cartItem.getCartId())
                .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "장바구니가 없습니다."));
        cart.updateSummary(cart.getTotalQuantity(), cart.getTotalPrice());
        cartRepository.saveCart(cart);
    }

    public List<CartItemModel> getAllCart(CartModel cart) {
         return Optional.of(cartRepository.findCartItemsByCartId(cart.getCartId()))
                 .filter(items -> !items.isEmpty())
                 .orElseThrow(() -> new CoreException(ErrorType.BAD_REQUEST, "장바구니가 비었습니다."));
    }

    public void clearCart(CartModel cart) {
        cartRepository.clearCart(cart);
    }



}
