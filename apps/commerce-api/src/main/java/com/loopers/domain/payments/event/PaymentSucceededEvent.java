package com.loopers.domain.payments.event;

public record PaymentSucceededEvent(
        String orderId,
        String paymentId
) {
}
