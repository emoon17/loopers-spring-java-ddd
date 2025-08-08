package com.loopers.infrastructure.coupon;

import com.loopers.domain.coupon.CouponModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CouponJpaRepository extends JpaRepository<CouponModel, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM CouponModel p WHERE p.couponId = :couponId")
    Optional<CouponModel> findCouponByCouponIdWithLock(String couponId);
}
