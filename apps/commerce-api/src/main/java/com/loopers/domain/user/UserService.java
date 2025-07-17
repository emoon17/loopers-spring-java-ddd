package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    @Transactional
    public UserModel register(String loginId, String email, String birth, String gender){
        if(userRepository.existsByLoginId(loginId)){
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 존재하는 ID 입니다.");
        }

        UserModel user = new UserModel(loginId, email, birth, gender);
        return userRepository.save(user);
    }

    public UserModel getUserByLoginId(String loginId){
        return userRepository.findByLoginId(loginId)
                .orElse(null);
    }

}
