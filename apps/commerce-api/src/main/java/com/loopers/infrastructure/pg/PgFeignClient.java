package com.loopers.infrastructure.pg;

import com.loopers.interfaces.api.payments.PaymentsDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name="pgClient", url = "${pg.base.url}")
public interface PgFeignClient {

    /**PG가 쓰는 dto*/
    record PgPaymentsDto (
            String transactionId,
            String orderId,
            Long amount,
            String cardType,
            String cardNo,
            String status,
            String callbackUrl
    ) {}

    @PostMapping("/api/v1/payments")
    PgPaymentsDto request(@RequestHeader("X-USER-ID") String userId, @RequestBody PgPaymentsDto req);

    @GetMapping("/api/v1/payments/{transactionId}")
    PgPaymentsDto requestByTransactionId(@RequestHeader("X-USER-ID") String userId, @PathVariable("transactionId") String transactionId);

    @GetMapping("/api/v1/payments")
    List<PaymentsDto> findOrderId(@RequestHeader("X-USER-ID") String userId, @RequestParam String orderId);



}
