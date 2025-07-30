package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Slf4j
@Getter
@Table(name = "brand")
public class BrandModel  {
    @Id
    private String brandId;
    private String brandName;

    protected BrandModel() {};
    public BrandModel(String brandId, String brandName) {
        this.brandId = brandId;
        this.brandName = brandName;
    }
}
