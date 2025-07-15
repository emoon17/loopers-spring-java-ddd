package com.loopers.domain.user;

import java.util.Optional;

public interface UserRepository {

    UserModel save(UserModel user);
    Optional<UserModel> findByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

}
