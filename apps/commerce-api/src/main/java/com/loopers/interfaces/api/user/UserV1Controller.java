package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserFacade;
import com.loopers.application.user.UserInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserV1Controller implements UserV1ApiSpec {
    private final UserFacade userFacade;

    @PostMapping
    @Override
    public ApiResponse<UserV1Dto.UsersResponse> registerUser(
            @RequestBody UserV1Dto.RegisterUserRequest request
    ) {
        UserInfo userInfo = userFacade.register(
                request.loginId(),
                request.email(),
                request.brith(),
                request.gender()
        );

        return ApiResponse.success(UserV1Dto.UsersResponse.from(userInfo));
    }

    @GetMapping("/me")
    @Override
    public ApiResponse<UserV1Dto.UsersResponse> getUsers(
            @RequestHeader("X-USER-ID") String loginId) {

        UserInfo userInfo = userFacade.getUserInfoByLoginId(loginId);
        return ApiResponse.success(UserV1Dto.UsersResponse.from(userInfo));
    }

}
