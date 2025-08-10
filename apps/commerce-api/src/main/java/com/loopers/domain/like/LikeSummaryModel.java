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
        name = "like_summary"
)
public class LikeSummaryModel extends BaseEntity {
    private String productId;

    private int totalLikeCount;

//    @Version
//    private Long version;

    protected LikeSummaryModel() {}

    public LikeSummaryModel(String productId, int totalLikeCount) {
        this.productId = productId;
        this.totalLikeCount = totalLikeCount;
    }

    public void increase() {
        this.totalLikeCount++;
    }

    public void decrease() {
        if (this.totalLikeCount > 0) {
            this.totalLikeCount--;
        }

    }
}
