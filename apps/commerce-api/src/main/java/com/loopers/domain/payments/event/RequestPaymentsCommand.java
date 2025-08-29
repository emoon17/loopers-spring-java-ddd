package com.loopers.domain.payments.event;

public record RequestPaymentsCommand(
        String orderId,
        String loginId,
        Long finalPrice,
        String cardType,
        String cardNo,
        String callbackUrl
) {
}
