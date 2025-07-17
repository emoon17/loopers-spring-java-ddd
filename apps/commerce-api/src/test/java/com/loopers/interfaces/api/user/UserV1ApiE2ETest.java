package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
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

@Slf4j
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserV1ApiE2ETest {

    @Autowired
    private TestRestTemplate testRestTemplate ;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;


    @AfterEach
    void tearDown() {
        databaseCleanUp.truncateAllTables();
    }

    private static final String ENDPOINT = "/api/v1/users";

    @DisplayName("POST /api/v1/users")
    @Nested
    class Register {


        @DisplayName("회원 가입이 성공한 경우, 생성된 유저 정보를 응답으로 반환한다.")
        @Test
        void returnUserInfo_whenRegisterIsSuccessful() {
            // arrange
            UserV1Dto.RegisterUserRequest request = new UserV1Dto.RegisterUserRequest(
                    "test123",
                    "test@test.com",
                    "1999-01-01",
                    "M"
            );

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UsersResponse>> responseType =
                    new ParameterizedTypeReference<ApiResponse<UserV1Dto.UsersResponse>>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UsersResponse>> response =
                    testRestTemplate.exchange(
                            ENDPOINT,
                            HttpMethod.POST,
                            new HttpEntity<>(request),
                            responseType
                    );
            // assert
            assertAll(
                    () -> assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK),
                    () -> assertThat(response.getBody()).isNotNull(),
                    () -> assertThat(response.getBody().data().email()).isEqualTo(request.email())
            );

        }


        @DisplayName("회원 가입 시에 성별이 없을 경우, `400 Bad Request` 응답을 반환한다.")
        @Test
        void throwException_whenGenderIsMissing() {
            // arrange
            UserV1Dto.RegisterUserRequest request = new UserV1Dto.RegisterUserRequest(
                    "test1234",
                    "test@test.com",
                    "1999-01-01",
                    ""
            );

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UsersResponse>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UsersResponse>> response =
                    testRestTemplate.exchange(
                            ENDPOINT,
                            HttpMethod.POST,
                            new HttpEntity<>(request),
                            responseType
                    );


            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }



    }

    @DisplayName("GET /api/v1/users/me")
    @Nested
    class GetUser {

        @DisplayName("내 정보 조회에 성공할 경우, 해당하는 유저 정보를 응답으로 반환한다.")
        @Test
        void returnUserInfo_whenGetUserIsSuccessful() {

            // arrange
            String requestUrl = ENDPOINT + "/me";
            UserModel savedUser = userJpaRepository.save(
                    new UserModel("test123", "test@test.com", "1999-01-01", "M")
            );

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", savedUser.getLoginId());
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UsersResponse>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UsersResponse>> response =
                    testRestTemplate.exchange(
                            requestUrl,
                            HttpMethod.GET,
                            entity,
                            responseType
                    );
            // assert
            assertAll(
                    () -> assertThat(response.getBody().data().email()).isEqualTo(savedUser.getEmail()),
                    () -> assertThat(response.getBody().data().birth()).isEqualTo(savedUser.getBirth()),
                    () -> assertThat(response.getBody().data().gender()).isEqualTo(savedUser.getGender())
            );
        }

        @DisplayName("존재하지 않는 ID 로 조회할 경우, `404 Not Found` 응답을 반환한다.")
        @Test
        void throwException_whenGetUserByLoginIdIsNotFound() {
            String requestUrl = ENDPOINT + "/me";

            // arrange
            userJpaRepository.save(
                    new UserModel("test123", "test@test.com", "1999-01-01", "M")
            );

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-USER-ID", "11122");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            // act
            ParameterizedTypeReference<ApiResponse<UserV1Dto.UsersResponse>> responseType =
                    new ParameterizedTypeReference<>() {};
            ResponseEntity<ApiResponse<UserV1Dto.UsersResponse>> response =
                    testRestTemplate.exchange(
                            requestUrl,
                            HttpMethod.GET,
                            entity,
                            responseType
                    );

            // assert
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }

    }


}
