package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.BrandModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.ZonedDateTime;

@Entity
@Slf4j
@Getter
@Table(name = "product")
public class ProductModel {

    @Id
    private String productId;
    private String productName;
    private String productDescription;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private BrandModel brand;
    private Long price;
    private Long stock;
    @Column(name = "created_at", nullable = false, updatable = false)
    private ZonedDateTime createdAt;
    @Column(name = "updated_at", nullable = false, updatable = false)
    private ZonedDateTime updatedAt;

    protected ProductModel() {};

    public ProductModel(String productId, String productName, String productDescription, BrandModel brand, Long price, Long stock, ZonedDateTime createdAt, ZonedDateTime updatedAt) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.brand = brand;
        this.price = price;
        if (stock < 0) {
            throw new CoreException(ErrorType.CONFLICT, "재고는 0 이상이어야 합니다.");
        }
        this.stock = stock;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public void decreaseStock(Long quantity) {
        if(stock < quantity) {
            throw new CoreException(ErrorType.CONFLICT, "재고가 부족합니다.");
        }

        stock -= quantity;
    }



}
