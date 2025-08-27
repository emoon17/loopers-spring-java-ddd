package com.loopers.application.user;

import com.loopers.domain.user.UserModel;

public record UserInfo(Long id, String loginId, String email, String birth, String gender) {
    public static UserInfo from(UserModel model) {
        return new UserInfo(
                model.getId(),
                model.getLoginId(),
                model.getEmail(),
                model.getBirth(),
                model.getGender());
    }
    public static UserModel from(UserInfo userInfo) {
        return new UserModel(
                userInfo.loginId,
                userInfo.email,
                userInfo.birth,
                userInfo.gender
        );
    }
}
