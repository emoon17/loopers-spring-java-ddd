package com.loopers.application.order;

import com.loopers.domain.order.OrderItemModel;

public record OrderItemInfo(
        String orderItemId,
        String productId,
        Long quantity,
        Long price
) {
    public static OrderItemInfo from(OrderItemModel item) {
        return new OrderItemInfo(
                item.getOrderItemId(),
                item.getProductId(),
                item.getQuantity(),
                item.getPrice()
        );
    }

}
