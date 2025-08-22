package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderModel;
import com.loopers.domain.order.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@RequiredArgsConstructor
@Component
public class OrderRepositoryImpl implements OrderRepository {

    private final OrderJpaRepository orderJpaRepository;
    private final OrderItemJpaRepository orderItemJpaRepository;


    @Override
    public void saveOrder(OrderModel order) {
        orderJpaRepository.save(order);
    }

    @Override
    public void saveOrderItems(List<OrderItemModel> orderItems) {
        orderItemJpaRepository.saveAll(orderItems);
    }

    @Override
    public OrderModel findByOrderId(String orderId) {
        return orderJpaRepository.findById(orderId).orElse(null);
    }
}
