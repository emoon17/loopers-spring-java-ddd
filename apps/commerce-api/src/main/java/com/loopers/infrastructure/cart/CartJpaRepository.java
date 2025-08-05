package com.loopers.infrastructure.cart;

import com.loopers.domain.cart.CartModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<CartModel, String> {

    Optional<CartModel> findCartByLoginId(String loginId);
    Optional<CartModel> findCartByCartId(String cartId);
    int findTotalQuantityByCartId(String cartId);
    int findTotalPriceByCartId(String cartId);
}
