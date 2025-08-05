package com.loopers.infrastructure.cart;

import com.loopers.domain.cart.CartItemModel;
import com.loopers.domain.cart.CartModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemJpaRepository extends JpaRepository<CartItemModel, String> {

    Optional<CartItemModel> findByCartIdAndProductId(String cartId, String productId);
    List<CartItemModel> findCartItemsByCartId(String cartId);


}
