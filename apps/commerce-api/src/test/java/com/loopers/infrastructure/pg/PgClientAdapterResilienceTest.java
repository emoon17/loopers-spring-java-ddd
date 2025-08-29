package com.loopers.infrastructure.pg;

import com.loopers.domain.payments.PaymentStatus;
import com.loopers.domain.payments.port.PgClientPort;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.net.SocketTimeoutException;
import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {
        "resilience4j.retry.instances.pgRetry.retry-exceptions[0]=feign.RetryableException",
        "resilience4j.retry.instances.pgRetry.retry-exceptions[1]=java.net.SocketTimeoutException",
        "resilience4j.retry.instances.pgRetry.retry-exceptions[2]=feign.FeignException",
        "resilience4j.retry.instances.pgRetry.retry-exceptions[3]=java.io.IOException",
        "resilience4j.retry.instances.pgRetry.retry-exceptions[4]=java.lang.RuntimeException" // 테스트 한정
})
@SpringBootTest
public class PgClientAdapterResilienceTest {

    @Autowired
    PgClientAdapter pgClientAdapter;
    @Autowired
    CircuitBreakerRegistry cbRegistry;
    @MockitoBean
    PgFeignClient pgFeign;

    @AfterEach
    void resetAll() {
        // 서킷 상태 원복 + 모킹 리셋
        cbRegistry.getAllCircuitBreakers().forEach(CircuitBreaker::reset);
        Mockito.reset(pgFeign);
    }


    private static feign.Request req() {
        return feign.Request.create(
                feign.Request.HttpMethod.POST,
                "/api/v1/payments",
                java.util.Collections.emptyMap(),
                new byte[0],
                java.nio.charset.StandardCharsets.UTF_8,
                new feign.RequestTemplate()
        );
    }
    private static RuntimeException timeout() {
        return new RuntimeException(new SocketTimeoutException("simulated"));
    }
    private static feign.FeignException feign500() {
        feign.Response resp = feign.Response.builder()
                .request(req()).status(500).reason("ISE")
                .headers(java.util.Collections.emptyMap())
                .body("{}", java.nio.charset.StandardCharsets.UTF_8)
                .build();
        return feign.FeignException.errorStatus("PgFeignClient#request", resp);
    }
    private static PgFeignClient.PgPaymentsDto okDto(String ord, String tx) {
        return new PgFeignClient.PgPaymentsDto(
                tx, ord, null, null, null, "SUCCESS", null
        );
    }



    @DisplayName("응답 지연에 대해 타임아웃을 설정하고, 실패시 적절한 예외처리를 한다.")
    @Nested
    class ThrowException {
        @Test
        void 재시도후_성공하면_SUCCESS와_txId가_반환된다(){
            when(pgFeign.request(anyString(), any()))
                    .thenThrow(feign500())
                    .thenThrow(timeout())
                    .thenReturn(okDto("order01", "tx01"));

            // act
            var out = pgClientAdapter.retrievePayments("login01",
                    new PgClientPort.PgPaymentsRequest(
                            "order01",
                            5000L,
                            "SAMSUNG",
                            "4111-1111-1111-1111",
                            "http://cb"
                    )
            );

            //assert
            verify(pgFeign,times(3)).request(anyString(), any());
            assertThat(out.status()).isEqualTo(PaymentStatus.SUCCESS);
            assertThat(out.transactionId()).isEqualTo("tx01");
            assertThat(out.orderId()).isEqualTo("order01");
        }

        @DisplayName("max-attempts 만큼 호출 후 연속 성공시 closed로 전환된다.")
        @Test
        void HALF_OPEN_허용된_성공횟수만큼_연속성공_후_CLOSED_전환(){
            // arrange
            CircuitBreaker cb = cbRegistry.circuitBreaker("pgCircuit");
            cb.transitionToOpenState(); // 테스트 편의를 위해 즉시 open으로 강제전환ㅎ다.
            cb.transitionToHalfOpenState(); // 바로 halfopen으로 전환해서 시험통과 상태로 만든다
            //act
            when(pgFeign.request(anyString(), any()))
                    .thenReturn(okDto("order01", "tx01"));
            int permits = cb.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState(); // halfopen - 닫힘으로 돌아가기 위해 설정에서 성공횟수를 가져온다.
            for(int i = 0; i < permits; i++) {
                pgClientAdapter.retrievePayments(
                        "login12",
                        new PgClientPort.PgPaymentsRequest(
                                "order01",
                                5000L,
                                "SAMSUNG",
                                "4111-1111-1111-1111",
                                "http://cb"
                        )
                );
            }
            // assert - 모든 허용 통화성공 -> closed로 전환되어야한다.
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.CLOSED);
        }

        @DisplayName("500번대 에러 발생시 open으로 전환된다.")
        @Test
        void HALF_OPEN_실패발생시_다시_OPEN_전환(){
            //arrange
            CircuitBreaker cb = cbRegistry.circuitBreaker("pgCircuit");
            cb.transitionToOpenState();
            cb.transitionToHalfOpenState();
            when(pgFeign.request(anyString(), any()))
                    .thenThrow(feign500());
            int permits = cb.getCircuitBreakerConfig().getPermittedNumberOfCallsInHalfOpenState();

            //act -  허용횟수만큼 돌리지만, 첫 호출에서 이미 실패가 나면 그 시점에 open으로 전환된다.
            for(int i = 0; i < permits; i++) {
                pgClientAdapter.retrievePayments(
                        "login12",
                        new PgClientPort.PgPaymentsRequest(
                                "order01",
                                5000L,
                                "SAMSUNG",
                                "4111-1111-1111-1111",
                                "http://cb"
                        )
                );
            }

            // assert
            assertThat(cb.getState()).isEqualTo(CircuitBreaker.State.OPEN);
        }

        @DisplayName("400번 에러는 retry없이 바로 FAILD 반환한다.")
        @Test
        void 배드리퀘스트_에러_발생_시_FALID_반환(){
            when(pgFeign.request(anyString(), any()))
                    .thenThrow(FeignException.errorStatus("PgFeignClient#request",
                            feign.Response.builder()
                                    .request(req())
                                    .status(400)
                                    .reason("Bad Request")
                                    .headers(Collections.emptyMap())
                                    .build()));

            var out = pgClientAdapter.retrievePayments(
                    "login12",
                    new PgClientPort.PgPaymentsRequest(
                            "order01",
                            5000L,
                            "SAMSUNG",
                            "4111-1111-1111-1111",
                            "http://cb"
                    )
            );

            verify(pgFeign,times(1)).request(anyString(), any());
            assertThat(out.status()).isEqualTo(PaymentStatus.FAILED);
        }
    }

}
