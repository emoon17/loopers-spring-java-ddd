package com.loopers.infrastructure.userCoupon;


import com.loopers.domain.userCoupon.UserCouponModel;
import com.loopers.domain.userCoupon.UserCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class UserCouponRepositoryImpl implements UserCouponRepository {
    private final UserCouponJapRepository userCouponjpaRepository;
    @Override
    public Optional<UserCouponModel> findUserCoupon(UserCouponModel userCouponModel) {
        return userCouponjpaRepository.findById(userCouponModel.getCouponId());
    }
}
