package com.loopers.infrastructure.point;

import com.loopers.domain.point.PointModel;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface PointJpaRepository extends JpaRepository<PointModel, Long> {

    Optional<PointModel> findPointByLoginId(String loginId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM PointModel p WHERE p.loginId = :loginId")
    Optional<PointModel> findByLoginIdWithLock(@Param("loginId") String loginId);


}
