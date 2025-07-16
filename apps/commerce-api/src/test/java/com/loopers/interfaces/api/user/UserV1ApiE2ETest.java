package com.loopers.interfaces.api.user;

import com.loopers.domain.user.UserModel;
import com.loopers.domain.user.UserService;
import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.ApiResponse;
import com.loopers.utils.DatabaseCleanUp;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserV1ApiE2ETest {

    /*
    *** E2E 테스트**
    - [o]  회원 가입이 성공할 경우, 생성된 유저 정보를 응답으로 반환한다.
    - [ ]  회원 가입 시에 성별이 없을 경우, `400 Bad Request` 응답을 반환한다.
    *
    * */


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

    @DisplayName("POST /api/v1/users")
    @Nested
    class Register {

        private static final String ENDPOINT = "/api/v1/users";

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



}
