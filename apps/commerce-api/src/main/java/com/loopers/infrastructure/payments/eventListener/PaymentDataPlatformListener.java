package com.loopers.infrastructure.payments.eventListener;

import com.loopers.domain.payments.event.PaymentFailedEvent;
import com.loopers.domain.payments.event.PaymentSucceededEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentDataPlatformListener {

    @EventListener
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        log.info("[DataPlatform] PaymentSucceededEvent: orderId={}, paymentId={}",
                event.orderId(), event.paymentId());
    }

    @EventListener
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.warn("[DataPlatform] PaymentFailedEvent: orderId={}, reason={}",
                event.orderId(), event.reason());
    }
}
