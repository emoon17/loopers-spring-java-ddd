package com.loopers.application.users;

import com.loopers.domain.users.UsersModel;

public record UsersInfo(Long id, String loginId, String email, String birth, String gender) {
    public static UsersInfo from(UsersModel model) {
        return new UsersInfo(
                model.getId(),
                model.getLoginId(),
                model.getEmail(),
                model.getBirth(),
                model.getGender());
    }
}
