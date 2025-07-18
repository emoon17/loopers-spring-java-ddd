package com.loopers.domain.point;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PointModelTest {
    @DisplayName("포인트를 충전할 때,")
    @Nested
    class Create {
        @DisplayName("0 이하의 정수로 포인트를 충전 시 실패한다.")
        @Test
        void throwBadRequest_whenChargeAmountIsZeroOrNegative() {
            // arrange
            PointModel pointModel = new PointModel("test123", 1000L);

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                    pointModel.chargePoint(-200L);
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
