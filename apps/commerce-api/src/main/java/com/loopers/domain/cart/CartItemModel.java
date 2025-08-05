package com.loopers.domain.cart;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Entity
@Slf4j
@Getter
@Table(name = "cart_item")
public class CartItemModel {
    @Id
    private String cartItemId;
    private String cartId;
    private String productId;
    private int quantity;
    private int price;

    protected CartItemModel() {}

    public CartItemModel(String cartItemId, String cartId, String productId, int quantity, int price) {
        this.cartItemId = cartItemId;
        this.cartId = cartId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }

    public void increaseQuantity(int quantityToAdd) {
        int unit = this.price / this.quantity;  // 기존 단가 저장
        this.quantity += quantityToAdd;
        this.price = unit * this.quantity;
    }

    public boolean isSameProduct(CartItemModel other) {
        return this.productId.equals(other.productId);
    }

    private int unitPrice() {
        return price / quantity;
    }
}
