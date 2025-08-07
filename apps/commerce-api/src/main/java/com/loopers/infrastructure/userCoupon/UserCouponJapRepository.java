package com.loopers.infrastructure.userCoupon;

import com.loopers.domain.userCoupon.UserCouponModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserCouponJapRepository extends JpaRepository<UserCouponModel, String> {
}
