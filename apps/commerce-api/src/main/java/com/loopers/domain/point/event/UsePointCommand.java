package com.loopers.domain.point.event;

public record UsePointCommand(
        String orderId,
        String loginId,
        Long useAmount,
        Long totalPriceBefore
) {
}
