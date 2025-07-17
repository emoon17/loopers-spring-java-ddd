package com.loopers.domain.point;

import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.interfaces.api.point.PointV1Dto;
import com.loopers.utils.DatabaseCleanUp;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
public class PointServiceIntegrationTest {

    @Autowired
    private PointService pointService;

    @SpyBean
    private PointJpaRepository pointJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    public void tearDown() { databaseCleanUp.truncateAllTables(); }

    @DisplayName("포인트 조회할 때,")
    @Nested
    class GetPoint{
        @DisplayName("해당 ID 의 회원이 존재할 경우, 보유 포인트가 반환된다.")
        @Test
        void returnTotalAmount_whenLoginIdIsExists(){
            // arrange
            PointModel savepoint = pointJpaRepository.save(
                    new PointModel(
                            "test1234",
                            1000L,
                            10000L
                    )
            );

            // act
            pointService.getPointModelByLoginId(savepoint.getLoginId());

            // verify
            verify(pointJpaRepository).findPointByLoginId(savepoint.getLoginId());

        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnNull_whenLoginIdIsNotExists(){
            // arrange
            pointJpaRepository.save(
                    new PointModel(
                            "test1234",
                            1000L,
                            10000L
                    )
            );

            // act
            PointModel result = pointService.getPointModelByLoginId("1111");

            // assert
            assertThat(result).isNull();
        }
    }
}
