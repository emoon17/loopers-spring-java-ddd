package com.loopers.domain.users;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsersService {

    private final UsersRepository usersRepository;


    @Transactional
    public UsersModel register(String loginId, String email, String birth, String gender){
        if(usersRepository.existsByLoginId(loginId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 ID 입니다.");
        }

        UsersModel user = new UsersModel(loginId, email, birth, gender);
        return usersRepository.save(user);
    }
}
