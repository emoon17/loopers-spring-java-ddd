package com.loopers.application.order;

import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderStatus;

import java.util.List;

public record OrderInfo(
        String orderId,
        String loginId,
        OrderStatus status,
        String orderType,
        Long totalQuantity,
        Long totalPrice,
        String createdAt,
        String updatedAt,
        List<OrderItemInfo> items
) {
    public static OrderInfo from(OrderModel order, List<OrderItemInfo> items) {
        return new OrderInfo(
                order.getOrderId(),
                order.getLoginId(),
                order.getStatus(),
                order.getOrderType(),
                order.getTotalQuantity(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getUpdatedAt(),
                items
        );
    }
}
