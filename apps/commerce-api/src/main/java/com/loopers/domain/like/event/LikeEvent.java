package com.loopers.domain.like.event;

import javax.swing.*;
import java.time.Instant;

public record LikeEvent(
        String productId,
        String loginId,
        Action action,
        Instant occurredAt
) {
    public enum Action { LIKE, UNLIKE }

    public static LikeEvent liked(String productId, String loginId){
        return new LikeEvent(productId, loginId, Action.LIKE, Instant.now());
    }

    public static LikeEvent unliked(String productId, String loginId){
        return new LikeEvent(productId, loginId, Action.UNLIKE, Instant.now());
    }
}
