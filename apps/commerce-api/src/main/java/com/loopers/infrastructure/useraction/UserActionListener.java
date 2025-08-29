package com.loopers.infrastructure.useraction;

import com.loopers.domain.useraction.UserActionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserActionListener {

    // 추후 외부 전송 고려로 인해 새 스레드에서 실행하도록 했습ㄴ다
    @Async 
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUserAction(UserActionEvent event) {
        log.info("[UserAction] loginId={}, action={}, targetId={}",
                event.loginId(), event.actionType(), event.targetId());

    }
}
