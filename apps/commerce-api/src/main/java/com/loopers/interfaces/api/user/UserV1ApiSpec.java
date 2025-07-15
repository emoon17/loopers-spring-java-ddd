package com.loopers.interfaces.api.user;

import com.loopers.interfaces.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Users V1 API", description = "User 관련 API 입니다.")
public interface UserV1ApiSpec {

    @Operation(
            summary = "유저 등록",
            description = "새로운 유저를 등록합니다."
    )
    ApiResponse<UserV1Dto.UsersResponse> registerUser(
            @Schema(description = "유저 등록 요청 정보") UserV1Dto.RegisterUserRequest registerUserRequest
    );
}
