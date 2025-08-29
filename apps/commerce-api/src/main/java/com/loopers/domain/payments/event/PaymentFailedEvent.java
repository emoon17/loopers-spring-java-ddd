package com.loopers.domain.payments.event;

public record PaymentFailedEvent(
        String orderId,
        String userCouponId,
        String reason
) {
}
