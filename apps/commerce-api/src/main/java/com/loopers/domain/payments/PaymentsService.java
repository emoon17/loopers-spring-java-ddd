package com.loopers.domain.payments;

import com.loopers.domain.payments.event.PaymentFailedEvent;
import com.loopers.domain.payments.event.PaymentSucceededEvent;
import com.loopers.domain.payments.event.RequestPaymentsCommand;
import com.loopers.domain.payments.port.PgClientPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class PaymentsService {

    private final PaymentsRepository paymentsRepository;
    private final PgClientPort pgClientPort;
    private final ApplicationEventPublisher eventPublisher;

    public void save(PaymentsModel paymentsModel) {
        paymentsRepository.save(paymentsModel);
    }

    public List<PaymentsModel> findPendingOrders() {
        return paymentsRepository.findPendingOrders(PaymentStatus.PENDING);
    }

    @Transactional
    public void handle(RequestPaymentsCommand command) {
        // 1. 결제시도 저장
        PaymentsModel paymentsModel = PaymentsModel.newPending(
                command.loginId(),
                command.orderId(),
                command.finalPrice()
        );
        paymentsRepository.save(paymentsModel);

        // 2. pg 요청
        afterCommit(() -> {
            try {
                var res = pgClientPort.retrievePayments(
                        command.loginId(),
                        new PgClientPort.PgPaymentsRequest(
                                paymentsModel.getOrderId(),
                                paymentsModel.getAmount(),
                                command.cardType(),
                                command.cardNo(),
                                command.callbackUrl()
                        )
                );
                if (res.transactionId() != null) {
                    paymentsModel.attachTransaction(res.transactionId());
                    paymentsModel.applyStatus(res.status());
                    paymentsRepository.save(paymentsModel);
                }
                eventPublisher.publishEvent(
                        new PaymentSucceededEvent(command.orderId(), paymentsModel.getPaymentId())
                );
            } catch (Exception e) {
                eventPublisher.publishEvent(
                        new PaymentFailedEvent(command.orderId(), "",  e.getMessage())
                );
            }
        });

    }


    /***
     * 콜백 : 상태만 반영(주문생성안함)
     *
     */
    @Transactional
    public PaymentsModel applyCallback(String transactionId, PaymentStatus status) {
        PaymentsModel found = paymentsRepository.findById(transactionId);
        found.applyStatus(status);
        return paymentsRepository.save(found);
    }

    /**
     * PG로부터 동기화 후 최종 상태 반환
     * */
    @Transactional
    public PaymentStatus syncFromPgAndGetStatus(String loginId, String orderId) {
        var pg = pgClientPort.getTransactionIds(loginId, orderId);
        PaymentsModel latest = paymentsRepository.findByOrderIdLatest(orderId);

        if (latest.getTransactionId() == null && pg.transactionId() != null) {
            latest.attachTransaction(pg.transactionId());
        }

        latest.applyStatus(pg.status());
        paymentsRepository.save(latest);
        return latest.getStatus();
    }

    private void afterCommit(Runnable r) {
        org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(
                new org.springframework.transaction.support.TransactionSynchronization() {
                    @Override public void afterCommit() { r.run(); }
                }
        );
    }

}
