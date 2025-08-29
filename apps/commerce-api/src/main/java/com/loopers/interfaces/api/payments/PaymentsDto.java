package com.loopers.interfaces.api.payments;

public class PaymentsDto{

    public record PaymentsRecord(
            String pymentId,
            String orderId,
            String loginId,
            String cardType,
            String cardNo,
            Long amount,
            String status,
            String callbackUrl
    ){}
}
