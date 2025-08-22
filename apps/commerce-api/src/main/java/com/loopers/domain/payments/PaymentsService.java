package com.loopers.domain.payments;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;

    public void save(PaymentsModel paymentsModel) {
        paymentsRepository.save(paymentsModel);
    }

    public PaymentsModel findById(String paymentId) {
        return paymentsRepository.findById(paymentId);
    }

    public PaymentsModel findByOrderIdLatest(String orderId) {
        return paymentsRepository.findByOrderIdLatest(orderId);
    }

}
