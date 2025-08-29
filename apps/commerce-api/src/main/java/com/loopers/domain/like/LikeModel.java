package com.loopers.domain.like;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.like.event.LikeEvent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Slf4j
@Getter
@Table(
        name = "like_table",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_like_user_product",
                columnNames = {"login_id","product_id"}
        )
)
public class LikeModel {
    @Id
    private String likeId;
    private String productId;
    private String loginId;
    @Column(name="is_like")
    private boolean isLike;

    protected LikeModel() {}

    public LikeModel(String likeId, String productId, String loginId, boolean isLike) {
        this.likeId = likeId;
        this.productId = productId;
        this.loginId = loginId;
        this.isLike = isLike;
    }

    public LikeEvent like() {
        if (this.isLike) return null;
        this.isLike = true;
        return LikeEvent.liked(this.productId, this.loginId);

    }

    public LikeEvent unlike() {
        if (!this.isLike) return null;
        this.isLike = false;
        return LikeEvent.unliked(this.productId, this.loginId);
    }
}
