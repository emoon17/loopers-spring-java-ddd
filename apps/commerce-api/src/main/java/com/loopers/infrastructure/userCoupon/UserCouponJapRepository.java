package com.loopers.infrastructure.userCoupon;

import com.loopers.domain.userCoupon.UserCouponModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserCouponJapRepository extends JpaRepository<UserCouponModel, String> {
    Optional<UserCouponModel> findUserCouponByUserCouponId(String userCouponId);
}
