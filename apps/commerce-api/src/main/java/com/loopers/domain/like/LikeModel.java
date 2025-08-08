package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Slf4j
@Getter
@Table(
        name = "like_table"
)
public class LikeModel extends BaseEntity {
    private String likeId;
    private String productId;
    private String loginId;
    private boolean isLike;

//    @Version
//    private Long version;

    protected LikeModel() {}

    public LikeModel(String likeId, String productId, String loginId, boolean isLike) {
        this.likeId = likeId;
        this.productId = productId;
        this.loginId = loginId;
        this.isLike = isLike;
    }

    public void like() {
        this.isLike = true;
    }

    public void unlike() {
        this.isLike = false;
    }
}
