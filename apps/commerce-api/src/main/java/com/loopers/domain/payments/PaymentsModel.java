package com.loopers.domain.payments;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Entity
@Slf4j
@Getter
@Table(name = "payments_table")
public class PaymentsModel {

    @Id
    private String paymentId;
    private String orderId;
    private String transactionId;
    private String loginId;
    private String cardType;
    private String cardNo;
    private Long amount;
    private PaymentStatus status;
    @Column(name = "updated_at", nullable = false)
    private ZonedDateTime updatedAt;

    protected PaymentsModel() {}

    /** 결제 시도만 했을 때(성공 여부 필요 없음) */
    public static PaymentsModel newPending(String orderId, long amount){
        PaymentsModel pay = new PaymentsModel();
        pay.orderId = orderId;
        pay.amount = amount;
        pay.status = PaymentStatus.PENDING;
        pay.updatedAt = ZonedDateTime.now();
        return pay;
    }

    /** PG 응답 받은 후 transaction 셑잉*/
    public void attachTransaction(String transactionId){
        this.transactionId = transactionId;
        this.updatedAt = ZonedDateTime.now();
    }

    /** 콜백-조회로 확정 된 상태 반영*/
    public void applyStatus(PaymentStatus status){
        if(this.status == status) return;

        // 멱등 - success -> failed 되는 거 방지
        if(this.status == PaymentStatus.SUCCESS) return;
        this.status = status;
        this.updatedAt = ZonedDateTime.now();
    }


}
