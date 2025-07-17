package com.loopers.application.point;

import com.loopers.domain.point.PointModel;

public record PointInfo(String loginId, Long amount, Long totalAmount) {
    public static PointInfo from(PointModel point){
        return new PointInfo(
                point.getLoginId(),
                point.getAmount(),
                point.getTotalAmount()
        );
    }
}
