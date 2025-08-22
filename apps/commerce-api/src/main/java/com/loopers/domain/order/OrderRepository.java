package com.loopers.domain.order;

import org.hibernate.query.Order;

import java.util.List;

public interface OrderRepository {

    void saveOrder(OrderModel order);
    void saveOrderItems(List<OrderItemModel> orderItems);
    OrderModel findByOrderId(String orderId);
}
