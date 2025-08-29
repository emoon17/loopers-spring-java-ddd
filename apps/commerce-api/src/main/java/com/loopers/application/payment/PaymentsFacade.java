package com.loopers.application.payment;

import com.loopers.domain.payments.PaymentStatus;
import com.loopers.domain.payments.PaymentsModel;
import com.loopers.domain.payments.PaymentsService;
import com.loopers.domain.payments.port.PgClientPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@RequiredArgsConstructor
@Component
public class PaymentsFacade {

    private final PaymentsService paymentsService;
    private final PgClientPort pgClientPort;

    @Transactional
    public String startAttempt(String loginId, String orderId, long amount) {
        var paymentsModel = PaymentsModel.newPending(loginId, orderId, amount);
        paymentsService.save(paymentsModel);
        return paymentsModel.getPaymentId();
    }

    /**
     * 트랜잭션 밖에서 PG 요청 -> transactionId 저장(상태는 pending)
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void requestToPg(String userId, String paymentsId, String cardType, String cardNo, String callbackUrl) {
        var paymentsModel = paymentsService.findById(paymentsId);
        var res = pgClientPort.retrievePayments(userId, new PgClientPort.PgPaymentsRequest(
                paymentsModel.getOrderId(), paymentsModel.getAmount(), cardType, cardNo, callbackUrl
        ));

        // pg가 tx를 발급해줬다면 붙여둔다.
        if (res.transactionId() != null && (paymentsModel.getTransactionId() == null)) {
            paymentsModel.attachTransaction(res.transactionId());
            paymentsService.save(paymentsModel);
        }


    }

    /***
     * 콜백 : 상태만 반영(주문생성안함)
     *
     */
    @Transactional
    public void applyCallback(String transactionId, PaymentStatus status) {
        PaymentsModel found = paymentsService.findById(transactionId);
        if (found == null) return;
        found.applyStatus(status);
        paymentsService.save(found);
    }

//    public PaymentStatus getCurrnentStatus(String orderId) {
//        var latestStatus = paymentsService.findByOrderIdLatest(orderId);
//        return latestStatus.getStatus();
//    }

    /**
     * PG로부터 동기화 후 최종 상태 반환
     * */
    @Transactional
    public PaymentStatus syncFromPgAndgGetStatus(String loginId, String orderId) {
        var pg = pgClientPort.getTransactionIds(loginId, orderId);
        PaymentsModel paymentsLatest = paymentsService.findByOrderIdLatest(orderId);

        if(paymentsLatest.getTransactionId() == null && pg.transactionId() != null) {
            paymentsLatest.attachTransaction(pg.transactionId());
        }

        paymentsLatest.applyStatus(pg.status());
        paymentsService.save(paymentsLatest);
        return paymentsLatest.getStatus();


    }
//
//    /**스케줄러에서 사용 예정*/
//    public List<PaymentsModel> findPendings(String orderId) {
//        return
//    }


}
