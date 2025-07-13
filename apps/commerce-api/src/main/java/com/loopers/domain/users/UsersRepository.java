package com.loopers.domain.users;

import java.util.Optional;

public interface UsersRepository {

    UsersModel save(UsersModel user);
    Optional<UsersModel> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

}
