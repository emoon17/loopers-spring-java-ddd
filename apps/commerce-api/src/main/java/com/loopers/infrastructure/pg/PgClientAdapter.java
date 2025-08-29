package com.loopers.infrastructure.pg;


import com.loopers.domain.payments.PaymentStatus;
import com.loopers.domain.payments.port.PgClientPort;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

/**
 * 포트 구현체
 * - Feign으로 PG 호출
 * - Reslience4j로 Retry/서킷/타임아웃 적용
 * - 외부 status 문자열 -> 도메인 PaymentStatus 매핑
 * - 장애/ㅈㅣ연 시 fallback : 내부는 pending
 * */
@Component
@RequiredArgsConstructor
public class PgClientAdapter implements PgClientPort {

    private final PgFeignClient pgClient;

    private PgPaymentsResponse map(PgFeignClient.PgPaymentsDto d) {
        return new PgPaymentsResponse(d.transactionId(), d.orderId(), toStatus(d.status()));
    }

    private PaymentStatus toStatus(String s) {
        if (s==null) return PaymentStatus.PENDING;
        return switch (s) {
            case "SUCCESS" -> PaymentStatus.SUCCESS;
            case "FAIL" -> PaymentStatus.FAILED;
            case "REJECTED" -> PaymentStatus.REJECTED;
            case "PENDING" -> PaymentStatus.PENDING;
            default -> PaymentStatus.FAILED;
        };
    }

    @Override
    @Retry(name="pgRetry", fallbackMethod="fallbackRequest")
    @CircuitBreaker(name="pgCircuit")
    public PgPaymentsResponse retrievePayments(String userId, PgPaymentsRequest request) {
        try {
            var dto = pgClient.request(userId, new PgFeignClient.PgPaymentsDto(
                    null, request.orderId(), request.amount(), request.cardType(),
                    request.cardNo(), null, request.callbackUrl()
            ));
            return map(dto);

        } catch (FeignException e) {
            if (e.status() >= 400 && e.status() < 500) {
                // 400대만 FAILED 처리한다.
                return new PgPaymentsResponse(null, request.orderId(), PaymentStatus.FAILED);
            }
            throw e;        }
    }

    @Override
    @Retry(name="pgRetry")
    @CircuitBreaker(name="pgCircuit")
    public PgPaymentsResponse getTransactionIds(String userId, String transactionId) {
        return map(pgClient.requestByTransactionId(userId, transactionId));
    }

    public PgPaymentsResponse fallbackRequest(String userId, PgPaymentsRequest request, Throwable throwable) {
        return new PgPaymentsResponse(null, request.orderId(), PaymentStatus.PENDING);
    }
}
