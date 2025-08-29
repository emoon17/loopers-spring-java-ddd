package com.loopers.domain.coupon;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Slf4j
@Getter
@Table(name = "coupon")
public class CouponModel {
    @Id
    private String couponId;
    private CouponType couponType;
    private Long discountAmount;
    private Double discountRate;
    private TargetType targetType; // product/ brandId
    private String targetId;
    private String createdAt;
    private String expiredAt; // 만료일자

    protected CouponModel() {}

    public CouponModel(String couponId,
                       CouponType couponType,
                       Long discountAmount,
                       Double discountRate,
                       TargetType targetType,
                       String targetId,
                       String createdAt,
                       String expiredAt) {
        if (couponType == CouponType.FIXED && discountAmount == null) {
            throw new IllegalArgumentException("FIXED 쿠폰은 discountAmount가 필수입니다.");
        }

        if (couponType == CouponType.PERCENT && discountRate == null) {
            throw new IllegalArgumentException("PERCENT 쿠폰은 discountRate가 필수입니다.");
        }

        this.couponId = couponId;
        this.couponType = couponType;
        this.discountAmount = discountAmount;
        this.discountRate = discountRate;
        this.targetType = targetType;
        this.targetId = targetId;
        this.createdAt = createdAt;
        this.expiredAt = expiredAt;
    }

    public Long calculateDiscount(Long totalPrice) {
        return switch (couponType) {
            case FIXED -> discountAmount;
            case PERCENT -> Math.round(totalPrice * discountRate);
        };
    }

    public Long applyDiscount(Long totalPrice) {
        Long discount = calculateDiscount(totalPrice);
        Long finalPrice = totalPrice - discount;
        if(finalPrice < 0 ) finalPrice = 0L;
        return finalPrice;
    }

    public enum CouponType {
        FIXED, PERCENT
    }

    public enum TargetType {
        PRODUCT, BRAND
    }
}
