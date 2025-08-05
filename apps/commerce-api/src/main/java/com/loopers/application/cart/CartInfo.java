package com.loopers.application.cart;

import com.loopers.domain.cart.CartModel;

import java.util.List;

public record CartInfo(
        String cartId,
        String loginId,
        int totalQuantity,
        int totalPrice,
        List<CartItemInfo> items
) {
    public static CartInfo from(CartModel cart, List<CartItemInfo> items) {
        return new CartInfo(
                cart.getCartId(),
                cart.getLoginId(),
                cart.getTotalQuantity(),
                cart.getTotalPrice(),
                items
        );
    }
}
