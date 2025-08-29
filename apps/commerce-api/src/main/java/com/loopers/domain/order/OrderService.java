package com.loopers.domain.order;

import com.loopers.domain.order.event.CreatedOrderCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    public void saveOrder(OrderModel order) {
        orderRepository.saveOrder(order);
    }

    public void saveOrderItems(List<OrderItemModel> orderItems) {
        orderRepository.saveOrderItems(orderItems);
    }
    public OrderModel findOrderById(String orderId) {return orderRepository.findByOrderId(orderId);}

    @Transactional
    public OrderModel handle(CreatedOrderCommand command) {
        OrderModel order = OrderModel.create(command.loginId(), command.items(), command.orderType(), command.userCouponId());

        orderRepository.saveOrder(order);
        orderRepository.saveOrderItems(command.items());

        return order;
    }

    public List<OrderItemModel> findOrderItems(String orderid) {
        return orderRepository.findOrderItems(orderid);
    }


}
