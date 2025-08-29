package com.loopers.domain.userCoupon;

import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Slf4j
@Getter
@Table(name = "user_coupon")
public class UserCouponModel {
    @Id
    private String userCouponId;
    private String loginId;
    private String couponId;
    private boolean used;
    private String issuedAt; // 사용일자

    @Version
    private Long version;

    protected UserCouponModel() {}

    public UserCouponModel(String userCouponId, String loginId, String couponId, String issuedAt) {
        this.userCouponId = userCouponId;
        this.loginId = loginId;
        this.couponId = couponId;
        this.used = false;
        this.issuedAt = issuedAt;
    }


    public void validateOwner(String loginId) {
        if (!this.loginId.equals(loginId)) {
            throw new CoreException(ErrorType.NOT_FOUND, "본인의 쿠폰이 아닙니다.");
        }
    }

    public void validateNotUsed() {
        if (used) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }
    }

    public void use() {
        validateNotUsed();
        this.used = true;
    }

    public void restore() {
        if (!this.used) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용 가능 상태입니다.");
        }
        this.used = false;
    }
}
