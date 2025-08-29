package com.loopers.domain.order.event;

import com.loopers.domain.order.OrderItemModel;

import java.util.List;

public record CreatedOrderCommand(
        String loginId,
        List<OrderItemModel> items,
        String orderType,
        String userCouponId
) {
}
