package com.loopers.interfaces.api.point;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@Tag(name = "Point V1 API", description = "Point 관련 API 입니다.")
public interface PointV1ApiSpec {

    @Operation(
            summary = "포인트 조회",
            description = "등록된 유저의 포인트를 조회합니다."
    )
    ApiResponse<PointV1Dto.PointResponse> getPoints(
            @RequestHeader("X-USER-ID")
            @Schema(description = "유저 식별자 (loginId)", example = "test123")
            String loginId
    );

    @Operation(
            summary = "포인트 충전",
            description = "등록된 유저의 포인트를 충전합니다."
    )
    ApiResponse<PointV1Dto.PointResponse> chargePoints(
            @RequestBody PointV1Dto.PointChargeRequest request
    );
}
