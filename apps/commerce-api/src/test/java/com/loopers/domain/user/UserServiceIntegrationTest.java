package com.loopers.domain.user;

import com.loopers.infrastructure.user.UserJpaRepository;
import com.loopers.interfaces.api.user.UserV1Dto;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import com.loopers.utils.DatabaseCleanUp;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

@Slf4j
@SpringBootTest
public class UserServiceIntegrationTest {
    @Autowired
    private UserService userService;

    @SpyBean
    private UserJpaRepository userJpaRepository;

    @Autowired
    private DatabaseCleanUp databaseCleanUp;

    @AfterEach
    public void tearDown() {
        databaseCleanUp.truncateAllTables();
    }


    @DisplayName("User를 생성할 때,")
    @Nested
    class Register {
        @DisplayName("회원가입 시 User가 저장된다.")
        @Test
        void saveUser_whenRegisterUser() {
            // arrange
            UserV1Dto.RegisterUserRequest request = new UserV1Dto.RegisterUserRequest(
                    "test1",
                     "test1@test.test",
                     "1999-01-01",
                    "M"
            );

            // act
            userService.register(request.loginId(), request.email(), request.brith(), request.gender());

            // verify
            verify(userJpaRepository).save(org.mockito.Mockito.any(UserModel.class));

        }

        @DisplayName("이미 가입된 ID로 회원가입 시도 시, 실패한다.")
        @Test
        void throwException_whenRegisterUserWithDuplicateLoginId() {
            // arrange
            UserV1Dto.RegisterUserRequest request = new UserV1Dto.RegisterUserRequest(
                    "test1",
                    "test1@test.test",
                    "1999-01-01",
                    "M"
            );

            userService.register(
                    request.loginId(),
                    request.email(),
                    request.brith(),
                    request.gender()
            );

            // act
            UserV1Dto.RegisterUserRequest duplicateRequest = new UserV1Dto.RegisterUserRequest(
                    "test1",
                    "test1@test.test",
                    "1999-01-01",
                    "M"
            );

            CoreException result = assertThrows(CoreException.class, () -> {
                userService.register(
                        duplicateRequest.loginId(),
                        duplicateRequest.email(),
                        duplicateRequest.brith(),
                        duplicateRequest.gender()
                );
            });

            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);

        }
    }


    /**
     * ** 통합 테스트**
     *
     * - [ ]  해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.
     * - [ ]  해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.
     *
     */
    @DisplayName("User를 조회할 때,")
    @Nested
    class getUser{
        @DisplayName("해당 ID 의 회원이 존재할 경우, 회원 정보가 반환된다.")
        @Test
        void returnUser_whenLoginIdExists() {
            // arrange
            UserV1Dto.RegisterUserRequest request = new UserV1Dto.RegisterUserRequest(
                    "test1",
                    "test1@test.test",
                    "1999-01-01",
                    "M"
            );

            userService.register(
                    request.loginId(),
                    request.email(),
                    request.brith(),
                    request.gender()
            );

            // act
            userService.getUserByLoginId(request.loginId());

            // verify
            verify(userJpaRepository).findByLoginId(request.loginId());
        }

        @DisplayName("해당 ID 의 회원이 존재하지 않을 경우, null 이 반환된다.")
        @Test
        void returnNull_whenLoginIdDoesNotExist() {

            // arrange
            UserV1Dto.RegisterUserRequest request = new UserV1Dto.RegisterUserRequest(
                    "test1",
                    "test1@test.test",
                    "1999-01-01",
                    "M"
            );

            userService.register(
                    request.loginId(),
                    request.email(),
                    request.brith(),
                    request.gender()
            );

            // act
              UserModel result = userService.getUserByLoginId("test");

            // assert
            assertThat(result).isNull();
        }
    }
}
