package com.loopers.domain.product;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.BrandModel;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Slf4j
@Getter
@Table(name = "product")
public class ProductModel extends BaseEntity {

    private String productId;
    private String productName;
    private String productDescription;
    private String brandId;
    private int price;
    private int stock;

    protected ProductModel() {};

    public ProductModel(String productId, String productName, String productDescription, String brandId, int price, int stock) {
        this.productId = productId;
        this.productName = productName;
        this.productDescription = productDescription;
        this.brandId = brandId;
        this.price = price;
        if (stock < 0) {
            throw new CoreException(ErrorType.CONFLICT, "재고는 0 이상이어야 합니다.");
        }
        this.stock = stock;
    }

    public void decreaseStock(int quantity) {
        if(stock < quantity) {
            throw new CoreException(ErrorType.CONFLICT, "재고가 부족합니다.");
        }

        stock -= quantity;
    }



}
