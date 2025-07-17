package com.loopers.domain.point;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;

@Entity
@Getter
@Table(name="point")
public class PointModel extends BaseEntity {

    private String loginId;
    private Long amount;
    private Long totalAmount;

    protected PointModel() {}

    public PointModel(String loginId, Long amount, Long totalAmount) {
        this.loginId = loginId;
        this.amount = amount;
        this.totalAmount = totalAmount;
    }
}
