package com.loopers.domain.product.event;

import com.loopers.domain.order.OrderItemModel;

import java.util.List;

public record DecreaseStockCommand(
        String loginId,
        List<OrderItemModel> items
) {
}
