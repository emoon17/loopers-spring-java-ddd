package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name="point")
public class PointModel extends BaseEntity {

    private String loginId;
    private Long amount;

    protected PointModel() {}

    public PointModel(String loginId, Long amount) {
        this.loginId = loginId;
        this.amount = amount;
    }

    public void chargePoint(Long chargeAmount) {
        if(chargeAmount == null || chargeAmount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "충전 금액은 0보다 커야 합니다.");
        }

        this.amount += chargeAmount;
    }

    public void usePoint(Long useAmount) {
        if (useAmount == null || useAmount <= 0) {
            throw new CoreException(ErrorType.BAD_REQUEST, "사용 금액은 0보다 커야 합니다.");
        }

        if (this.amount < useAmount) {
            throw new CoreException(ErrorType.CONFLICT, "포인트가 부족합니다.");
        }

        this.amount -= useAmount;
    }


}
