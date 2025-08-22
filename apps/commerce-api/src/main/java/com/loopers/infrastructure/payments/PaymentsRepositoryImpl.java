package com.loopers.infrastructure.payments;

import com.loopers.domain.payments.PaymentsModel;
import com.loopers.domain.payments.PaymentsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class PaymentsRepositoryImpl implements PaymentsRepository {

    private final PaymentsJpaRepository paymentsJpaRepository;

    @Override
    public void save(PaymentsModel paymentsModel) {
        paymentsJpaRepository.save(paymentsModel);
    }

    @Override
    public PaymentsModel findById(String id) {
        return paymentsJpaRepository.findById(id).orElse(null);
    }

    @Override
    public PaymentsModel findByOrderIdLatest(String orderId) {
        return paymentsJpaRepository.findByOrderIdLatest(orderId);
    }
}
