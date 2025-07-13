package com.loopers.interfaces.api.users;

import com.loopers.application.users.UsersInfo;

public class UsersV1Dto {

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
        public static UsersResponse from(UsersInfo usersInfo) {
            return new UsersResponse(
                    usersInfo.id(),
                    usersInfo.loginId(),
                    usersInfo.email(),
                    usersInfo.birth(),
                    usersInfo.gender()
            );
        }
    }
}
