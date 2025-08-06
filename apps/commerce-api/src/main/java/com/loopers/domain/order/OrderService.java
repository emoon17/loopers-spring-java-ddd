package com.loopers.domain.order;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public void saveOrder(OrderModel order) {
        orderRepository.saveOrder(order);
    }

    public void saveOrderItems(List<OrderItemModel> orderItems) {
        orderRepository.saveOrderItems(orderItems);
    }


}
