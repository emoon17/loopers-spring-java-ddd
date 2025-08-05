package com.loopers.application.cart;

import com.loopers.domain.cart.CartItemModel;
import com.loopers.domain.cart.CartModel;
import com.loopers.domain.cart.CartService;
import com.loopers.domain.product.ProductModel;
import com.loopers.domain.user.UserModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class CartFacade {

    private final CartService cartService;


    public void addProductToCart(UserModel user, ProductModel product, CartItemModel cartItem) {
        // 1. User의 cart 가 존재하는지 확인
        CartModel cart = cartService.getOrCreateCart(user);

        // 2. cartItem에 있는 productId가 cart에 존재하는 지 확인
        cartService.addOrUpdateCartItem(cartItem, product);

    }

    public CartInfo getCartInfo(UserModel user) {
        // 1. 해당 user의 같은 카트 Id를 갖고 있는 cartItem 조회
        CartModel cart = cartService.getOrCreateCart(user);
        List<CartItemModel> cartItems = cartService.getAllCart(cart);

        // 2. cartInfo에 items리스트 담아서 반환
        List<CartItemInfo> itemInfos = cartItems.stream()
                .map(CartItemInfo::from)
                .toList();

        return CartInfo.from(cart, itemInfos);
    }

}
