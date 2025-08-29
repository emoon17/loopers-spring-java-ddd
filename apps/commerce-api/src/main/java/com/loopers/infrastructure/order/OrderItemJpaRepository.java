package com.loopers.infrastructure.order;

import com.loopers.domain.order.OrderItemModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderItemJpaRepository extends JpaRepository<OrderItemModel, String> {

    @Query("""
    select p from OrderItemModel p where p.orderId = :orderId
""")
    List<OrderItemModel> findOrderItems(String orderId);
}
