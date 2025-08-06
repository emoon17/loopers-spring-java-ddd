package com.loopers.application.cart;

import com.loopers.domain.cart.CartItemModel;

public record CartItemInfo(
        String cartItemId,
        String productId,
        Long quantity,
        Long price
) {
    public static CartItemInfo from(CartItemModel item) {
        return new CartItemInfo(
                item.getCartItemId(),
                item.getProductId(),
                item.getQuantity(),
                item.getPrice()
        );
    }
}
