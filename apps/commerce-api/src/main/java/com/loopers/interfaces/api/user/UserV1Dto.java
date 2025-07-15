package com.loopers.interfaces.api.user;

import com.loopers.application.user.UserInfo;

public class UserV1Dto {

    public record RegisterUserRequest(
            String loginId,
            String email,
            String brith,
            String gender
    ) {}

    public record UsersResponse(
            Long id,
            String loginId,
            String email,
            String birth,
            String gender
    ){
        public static UsersResponse from(UserInfo userInfo) {
            return new UsersResponse(
                    userInfo.id(),
                    userInfo.loginId(),
                    userInfo.email(),
                    userInfo.birth(),
                    userInfo.gender()
            );
        }
    }
}
