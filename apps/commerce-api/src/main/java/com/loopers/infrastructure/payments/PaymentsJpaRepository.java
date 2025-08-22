package com.loopers.infrastructure.payments;

import com.loopers.domain.payments.PaymentsModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PaymentsJpaRepository extends JpaRepository<PaymentsModel, String> {

    @Query("""
    select p from PaymentsModel p where p.orderId = :orderId order by p.updatedAt desc
""")
    PaymentsModel findByOrderIdLatest(String orderId);

}
