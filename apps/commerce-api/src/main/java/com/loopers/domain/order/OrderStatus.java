package com.loopers.domain.order;

public enum OrderStatus {

    WAITING_FOR_PAYMENT,  // 주문은 생성됐지만 결제 미확정
    PAID,                 // 결제 성공 → 확정
    PAYMENT_FAILED,       // 결제 실패
    CANCELED              // 취소 등
}
