package com.loopers.domain.order;

import com.loopers.domain.cart.CartItemModel;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.UUID;

@Entity
@Slf4j
@Getter
@Table(name = "order_item")
public class OrderItemModel {
    @Id
    private String orderItemId;
    private String orderId;
    private String productId;
    private Long quantity;
    private Long price;

    protected OrderItemModel() {}

    public OrderItemModel(String orderItemId, String orderId, String productId, Long quantity, Long price) {
        this.orderItemId = orderItemId;
        this.orderId = orderId;
        this.productId = productId;
        this.quantity = quantity;
        this.price = price;
    }
    public static OrderItemModel from(CartItemModel cartItem, String orderId) {
        return new OrderItemModel(
                UUID.randomUUID().toString(),
                orderId,
                cartItem.getProductId(),
                cartItem.getQuantity(),
                cartItem.getPrice()
        );
    }

    public static List<OrderItemModel> fromCartItems(List<CartItemModel> cartItems, String orderId) {
        return cartItems.stream()
                .map(cartItem -> OrderItemModel.from(cartItem, orderId))
                .toList();
    }

    public void assignOrderId(String orderId) {
        this.orderId = orderId;
    }}
