package com.loopers.application.users;

import com.loopers.domain.users.UsersModel;
import com.loopers.domain.users.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class UsersFacade {
    private final UsersService usersService;

    public UsersInfo register(String loginId, String email, String birth, String gender){
        UsersModel user = usersService.register(
                loginId,
                email,
                birth,
                gender
        );
        return UsersInfo.from(user);

    }

}
