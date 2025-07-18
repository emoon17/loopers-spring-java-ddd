package com.loopers.interfaces.api.point;

import com.loopers.application.point.PointInfo;
import com.loopers.domain.point.PointModel;
import com.loopers.infrastructure.point.PointJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PointV1ApiE2ETest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private PointJpaRepository pointJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private static final String ENDPOINT = "/api/v1/points";

    @DisplayName("GET /api/v1/points")
    @Nested
    class GetPoints{
        /**
         * **E2E 테스트**
         * - [ o ]  포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.
         * - [ o ]  `X-USER-ID` 헤더가 없을 경우, `400 Bad Request` 응답을 반환한다.
         * */

        @DisplayName("포인트 조회에 성공할 경우, 보유 포인트를 응답으로 반환한다.")
        @Test
        void returnPointsTotalAmount_whenGetPointsIsSuccessful(){
            // arrange
            PointModel savePoint = pointJpaRepository.save(
                    new PointModel(
                            "test1234",
                            1000L
                    )
            );

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", savePoint.getLoginId());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType =
                    new ParameterizedTypeReference<>(){};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    restTemplate.exchange(
                            ENDPOINT,
                            HttpMethod.GET,
                            entity,
                            responseType
            );

            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().amount()).isEqualTo(1000)
            );
        }

        @DisplayName("`X-USER-ID` 헤더가 없을 경우, `400 Bad Request` 응답을 반환한다.")
        @Test
        void throwBadRequestException_whenXUserIdIsMissing(){
            // arrange
            pointJpaRepository.save(
                    new PointModel(
                            "test1234",
                            1000L
                    )
            );
            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", "");
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType =
                    new ParameterizedTypeReference<>(){};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    restTemplate.exchange(
                            ENDPOINT,
                            HttpMethod.GET,
                            entity,
                            responseType

                    );

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @DisplayName("POST /api/v1/points/charge")
    @Nested
    class PointCharge{
        @DisplayName("존재하는 유저가 1000원을 충전할 경우, 충전된 보유 총량을 응답으로 반환한다.")
        @Test
        void returnTotalAmount_whenExistingUserChargesPoints() {
            // arrange
            PointModel savePoint = pointJpaRepository.save(
                    new PointModel(
                            "teet1234",
                            1000L
                    )
            );

            PointV1Dto.PointChargeRequest requestBody = new PointV1Dto.PointChargeRequest(
                    savePoint.getLoginId(),
                    1000L
            );

            // act
            String requestUrl = ENDPOINT + "/charge";
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType =
                    new ParameterizedTypeReference<>(){};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    restTemplate.exchange(
                            requestUrl,
                            HttpMethod.POST,
                            new HttpEntity<>(requestBody),
                            responseType
                    );

            // assert
            assertThat(response.getBody().data().amount()).isEqualTo(2000L);
        }

        @DisplayName("존재하지 않는 유저로 요청할 경우, 404 Not Found 응답을 반환한다.")
        @Test
        void throwNotFoundException_whenUserIdMissing(){
            // arrange
            String requestUrl = ENDPOINT + "/charge";

            pointJpaRepository.save(
                    new PointModel(
                            "teet1234",
                            1000L
                    )
            );
            PointV1Dto.PointChargeRequest requestBody = new PointV1Dto.PointChargeRequest(
                    "1111",
                    1000L
            );

            // act
            ParameterizedTypeReference<ApiResponse<PointV1Dto.PointResponse>> responseType =
                    new ParameterizedTypeReference<>(){};
            ResponseEntity<ApiResponse<PointV1Dto.PointResponse>> response =
                    restTemplate.exchange(
                            requestUrl,
                            HttpMethod.POST,
                            new HttpEntity<>(requestBody),
                            responseType
                    );

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }
}
