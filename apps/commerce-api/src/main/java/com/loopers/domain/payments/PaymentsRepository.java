package com.loopers.domain.payments;

public interface PaymentsRepository {

    void save(PaymentsModel paymentsModel);
    PaymentsModel findById(String id);
    PaymentsModel findByOrderIdLatest(String orderId);
}
