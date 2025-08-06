package com.loopers.domain.order;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Slf4j
@Getter
@Table(name = "order_table")
public class OrderModel {
    @Id
    private String orderId;
    private String loginId;
    private String status;
    private String orderType; // 장바구니 -> 오더 , 바로 오더
    private Long totalQuantity;
    private Long totalPrice;
    private String createdAt;
    private String updatedAt;

    protected OrderModel() {}

    public OrderModel(String orderId, String loginId, String status, String orderType, Long totalQuantity, Long totalPrice, String createdAt, String updatedAt) {
        this.orderId = orderId;
        this.loginId = loginId;
        this.status = status;
        this.orderType = orderType;
        this.totalQuantity = totalQuantity;
        this.totalPrice = totalPrice;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static OrderModel create(String loginId, List<OrderItemModel> items, String orderType) {
        Long totalPrice = items.stream()
                .mapToLong(item -> item.getPrice() * item.getQuantity())
                .sum();
        Long totalQuantity = items.stream()
                .mapToLong(OrderItemModel::getQuantity)
                .sum();
        String now = LocalDateTime.now().toString();

        return new OrderModel(
                UUID.randomUUID().toString(),
                loginId,
                "",
                orderType,
                totalQuantity,
                totalPrice,
                now,
                now
        );
    }

    public long getTotalPrice() {
        return this.totalPrice;
    }

}
