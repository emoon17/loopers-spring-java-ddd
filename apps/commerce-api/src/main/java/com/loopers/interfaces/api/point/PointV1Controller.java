package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointFacade;
import com.loopers.application.point.PointInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/points")
public class PointV1Controller implements PointV1ApiSpec{

    private final PointFacade pointFacade;

    @GetMapping
    @Override
    public ApiResponse<PointV1Dto.PointResponse> getPoints(
            @RequestHeader("X-USER-ID")  String loginId
    ) {
      PointInfo pointInfo = pointFacade.getPointInfoByLoginId(loginId);
      return ApiResponse.success(PointV1Dto.PointResponse.from(pointInfo));
    }

    @PostMapping("/charge")
    @Override
    public ApiResponse<PointV1Dto.PointResponse> chargePoints(
            @RequestBody PointV1Dto.PointChargeRequest request) {
        PointInfo pointInfo = pointFacade.chargePoint(request.loginId(), request.amount());
        return ApiResponse.success(PointV1Dto.PointResponse.from(pointInfo));

    }


}
