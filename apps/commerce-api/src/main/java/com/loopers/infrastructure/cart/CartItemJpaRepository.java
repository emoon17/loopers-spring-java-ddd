package com.loopers.infrastructure.cart;

import com.loopers.domain.cart.CartItemModel;
import com.loopers.domain.cart.CartModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CartItemJpaRepository extends JpaRepository<CartItemModel, String> {

    Optional<CartItemModel> findByCartIdAndProductId(String cartId, String productId);
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CartItemModel p WHERE p.cartId = :cartId")
    List<CartItemModel> findCartItemsByCartIdWithLock(String cartId);


}
