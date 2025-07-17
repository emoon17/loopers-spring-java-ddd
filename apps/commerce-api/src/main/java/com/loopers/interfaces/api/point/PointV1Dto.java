package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointInfo;

public class PointV1Dto {

    public record PointRequest(
            String loginId,
            Long amount, // 충전 금액
            Long totalAmount // 총 충전 금액
    ){}

    public record PointResponse(
            String loginId,
            Long amount,
            Long totalAmount
    ){
        public static PointResponse from(PointInfo pointInfo) {
            return new PointResponse(
                    pointInfo.loginId(),
                    pointInfo.amount(),
                    pointInfo.totalAmount()
            );
        };
    }
}
