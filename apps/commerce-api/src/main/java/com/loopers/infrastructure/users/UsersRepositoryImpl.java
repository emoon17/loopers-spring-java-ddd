package com.loopers.infrastructure.users;

import com.loopers.domain.users.UsersModel;
import com.loopers.domain.users.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UsersRepositoryImpl implements UsersRepository {
    private final UsersJpaRepository usersJpaRepository;

    @Override
    public UsersModel save(UsersModel user) {
        usersJpaRepository.save(user);
        return user;
    }

    @Override
    public Optional<UsersModel> findByLoginId(String loginId) {
        return usersJpaRepository.findByLoginId(loginId);
    }

    @Override
    public boolean existsByLoginId(String loginId) {
        return usersJpaRepository.existsByLoginId(loginId);
    }
}
