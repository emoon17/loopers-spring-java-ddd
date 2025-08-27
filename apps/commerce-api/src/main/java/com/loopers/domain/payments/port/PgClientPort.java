package com.loopers.domain.payments.port;

import com.loopers.domain.payments.PaymentStatus;

/**
 * 도메인/애플리케이션이 "PG에 대해 원한다"고 선언하는 기능
 * 실제 HTTP 호출/회복 전략은 인프라 어댑터가 구현한다.
 * */
public interface PgClientPort {

    /** 나->pg : 결제 요청에 쓰는 데이터*/
    record PgPaymentsRequest(String orderId, long amount, String cardType, String cardNo, String callbackUrl){}
    // pg -> 나
    record PgPaymentsResponse(String transactionId, String orderId, PaymentStatus status){}

    PgPaymentsResponse retrievePayments(String loginId, PgPaymentsRequest request);
    PgPaymentsResponse getTransactionIds(String loginId, String orderId);

}
