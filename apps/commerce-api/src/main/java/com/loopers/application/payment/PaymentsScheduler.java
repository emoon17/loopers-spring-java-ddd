package com.loopers.application.payment;

import com.loopers.application.order.OrderFacade;
import com.loopers.domain.payments.PaymentStatus;
import com.loopers.domain.payments.PaymentsModel;
import com.loopers.domain.payments.PaymentsService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PaymentsScheduler {
    private final PaymentsService paymentsService;
    private final PaymentsFacade paymentsFacade;
    private final OrderFacade orderFacade;

    @Scheduled(fixedDelay = 300_000)
    public void sync(){
        //1. pending 결제건 조회
        List<PaymentsModel> pendingOrders = paymentsService.findPendingOrders();
        for(PaymentsModel payment : pendingOrders){
            //2. pg 상태 확인
            PaymentStatus status = paymentsFacade.syncFromPgAndgGetStatus(payment.getLoginId(), payment.getOrderId());
            //3. success -> 주문 확정 처리
            if(status == PaymentStatus.SUCCESS) {
                orderFacade.finalizeOrderAfterPayment(payment.getOrderId() ,status );
            }
        }
    }
}
