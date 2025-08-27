package com.loopers.infrastructure.payments;

import com.loopers.domain.payments.PaymentStatus;
import com.loopers.domain.payments.PaymentsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PaymentsJpaRepository extends JpaRepository<PaymentsModel, String> {

    @Query("""
    select p from PaymentsModel p where p.orderId = :orderId order by p.updatedAt desc
""")
    PaymentsModel findByOrderIdLatest(String orderId);
    @Query("""
    select p from PaymentsModel p where p.status = :status order by p.updatedAt desc
""")
    List<PaymentsModel> findPendingOrders(@Param("status") PaymentStatus status);

}
