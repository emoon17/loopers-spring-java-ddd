package com.loopers.domain.userCoupon;

import com.loopers.domain.user.UserModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
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

    protected UserCouponModel() {}

    public UserCouponModel(String userCouponId, String loginId, String couponId, String issuedAt) {
        this.userCouponId = userCouponId;
        this.loginId = loginId;
        this.couponId = couponId;
        this.used = false;
        this.issuedAt = issuedAt;
    }


    public void validateOwner(UserModel user) {
        if (!this.loginId.equals(user.getLoginId())) {
            throw new CoreException(ErrorType.NOT_FOUND, "본인의 쿠폰이 아닙니다.");
        }
    }

    public void validateNotUsed() {
        if (used && (issuedAt != null || issuedAt != "")) {
            throw new CoreException(ErrorType.BAD_REQUEST, "이미 사용된 쿠폰입니다.");
        }
    }

    public void use() {
        validateNotUsed();
        this.used = true;
    }
}
