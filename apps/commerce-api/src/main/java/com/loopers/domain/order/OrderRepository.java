package com.loopers.domain.order;

import java.util.List;

public interface OrderRepository {

    void saveOrder(OrderModel order);
    void saveOrderItems(List<OrderItemModel> orderItems);
}
