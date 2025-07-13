package com.loopers.interfaces.api.users;

import com.loopers.application.users.UsersFacade;
import com.loopers.application.users.UsersInfo;
import com.loopers.interfaces.api.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UsersV1Controller implements UsersV1ApiSpec {
    private final UsersFacade usersFacade;

    @PostMapping("/register")
    @Override
    public ApiResponse<UsersV1Dto.UsersResponse> registerUser(
            @RequestBody UsersV1Dto.RegisterUserRequest request
    ) {
        UsersInfo usersInfo = usersFacade.register(
                request.loginId(),
                request.email(),
                request.brith(),
                request.gender()
        );

        return ApiResponse.success(UsersV1Dto.UsersResponse.from(usersInfo));
    }
}
