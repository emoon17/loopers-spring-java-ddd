package com.loopers.infrastructure.payments.dataPlatform;

import com.loopers.domain.coupon.CouponService;
import com.loopers.domain.order.OrderItemModel;
import com.loopers.domain.order.OrderService;
import com.loopers.domain.payments.event.PaymentFailedEvent;
import com.loopers.domain.payments.event.PaymentSucceededEvent;
import com.loopers.domain.point.PointService;
import com.loopers.domain.product.ProductService;
import com.loopers.domain.userCoupon.UserCouponService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 결제 성공/실패 이벤트 리스너
 * - SUCCESS → 주문 확정
 * - FAILED  → 주문 실패 처리 + 보상 트랜잭션 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCompensationListener {

    private final OrderService orderService;
    private final ProductService productService;
    private final PointService pointService;
    private final UserCouponService userCouponService;


    @EventListener
    @Transactional
    public void onPaymentSucceeded(PaymentSucceededEvent event) {
        log.info("[Listener] 결제 성공 - orderId={}", event.orderId());

        // 재고/포인트/쿠폰은 이미 차감되었으므로 확정만 해주면 됨 (추가 작업 없을 수도 있음)
    }

    @EventListener
    @Transactional
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.warn("[Listener] 결제 실패 - orderId={}, reason={}",
                event.orderId(), event.reason());

        // 보상 트랜잭션 실행
        List<OrderItemModel> items = orderService.findOrderItems(event.orderId());
        for(OrderItemModel item : items) {
            productService.restoreStock(item.getProductId(), item.getQuantity());

        }
        pointService.restorePoints(event.orderId());
        userCouponService.restoreCoupon(event.userCouponId());
    }
}
