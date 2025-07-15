package com.loopers.application.user;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UserFacade {
    private final UserService userService;

    public UserInfo register(String loginId, String email, String birth, String gender){
        UserModel user = userService.register(
                loginId,
                email,
                birth,
                gender
        );
        return UserInfo.from(user);

    }

}
