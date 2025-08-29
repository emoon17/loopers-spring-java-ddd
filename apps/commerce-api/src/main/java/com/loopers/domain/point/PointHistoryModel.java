package com.loopers.domain.point;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
@Table(name = "point_history")
public class PointHistoryModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pointHistoryId;
    private String orderId;
    private String loginId;
    private Long usedAmount;
    private String reason;

    protected PointHistoryModel() {}

    public PointHistoryModel(String orderId, String loginId, Long usedAmount, String reason) {
        this.orderId = orderId;
        this.loginId = loginId;
        this.usedAmount = usedAmount;
        this.reason = reason;
    }

    public static PointHistoryModel used(String orderId, String loginId, Long amount) {
        return new PointHistoryModel( orderId, loginId, amount, "USE");
    }

    public static PointHistoryModel restored(String orderId, String loginId, Long amount) {
        return new PointHistoryModel(orderId, loginId, amount, "RESTORE");
    }
}
