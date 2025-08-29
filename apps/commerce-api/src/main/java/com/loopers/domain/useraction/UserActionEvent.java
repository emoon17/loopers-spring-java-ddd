package com.loopers.domain.useraction;

import java.time.ZonedDateTime;

public record UserActionEvent(
        String loginId,
        UserActionType actionType,
        String targetId,
        ZonedDateTime timestamp
) {
}
