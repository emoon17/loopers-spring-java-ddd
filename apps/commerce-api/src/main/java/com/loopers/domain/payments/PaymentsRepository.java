package com.loopers.domain.payments;

import java.util.List;

public interface PaymentsRepository {

    void save(PaymentsModel paymentsModel);
    PaymentsModel findById(String id);
    PaymentsModel findByOrderIdLatest(String orderId);
    List<PaymentsModel> findPendingOrders(PaymentStatus status);
}
