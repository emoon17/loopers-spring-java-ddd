package com.loopers.infrastructure.cart;

import com.loopers.domain.cart.CartModel;
import com.loopers.domain.cart.CartRepository;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CartJpaRepository extends JpaRepository<CartModel, String> {

    Optional<CartModel> findCartByLoginId(String loginId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CartModel p  WHERE p.loginId = :loginId")
    Optional<CartModel> findCartByLoginIdWithLock(String loginId);
    Optional<CartModel> findCartByCartId(String cartId);
    int findTotalQuantityByCartId(String cartId);
    int findTotalPriceByCartId(String cartId);
}
