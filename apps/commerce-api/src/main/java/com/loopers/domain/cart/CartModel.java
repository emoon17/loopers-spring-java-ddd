package com.loopers.domain.cart;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.UUID;

@Entity
@Slf4j
@Getter
@Table(name = "cart")
public class CartModel {
    @Id
    private String cartId;
    private String loginId;
    private int totalQuantity;
    private int totalPrice;

    protected CartModel() {}

    public CartModel(String cartId, String loginId, int totalQuantity, int totalPrice) {
        this.cartId = cartId;
        this.loginId = loginId;
        this.totalQuantity = totalQuantity;
        this.totalPrice = totalPrice;
    }

    public static CartModel create(String loginId) {
        return new CartModel(UUID.randomUUID().toString(), loginId, 0, 0);
    }

    public void updateSummary(int totalQuantity, int totalPrice) {
        this.totalQuantity = totalQuantity;
        this.totalPrice = totalPrice;
    }
}
