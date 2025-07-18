package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointInfo;

public class PointV1Dto {

    public record PointChargeRequest(
            String loginId,
            Long amount // 충전 금액
    ){}

    public record PointResponse(
            String loginId,
            Long amount
    ){
        public static PointResponse from(PointInfo pointInfo) {
            return new PointResponse(
                    pointInfo.loginId(),
                    pointInfo.amount()
            );
        };
    }
}
