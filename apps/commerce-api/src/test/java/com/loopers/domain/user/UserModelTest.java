package com.loopers.domain.user;

import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class UserModelTest {
    @DisplayName("User 객체를 생성할 때,")
    @Nested
    class Create{
        @DisplayName("ID가 영문 및 숫자 10자 이내 형식에 맞지 않으면 User 객체 생성에 실패한다.")
        @Test
        void throwBadRequestException_whenLoginIdFormatIsInvalid(){
            // arrange
            String loginId = "testtest1234";

            // act
            CoreException result = assertThrows(CoreException.class, () -> {
                new UserModel(
                        loginId,
                        "email@email.com",
                        "1999-01-01",
                        "W"
                );
            });

            // assert
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("이메일이 xx@yy.zz 형식에 맞지 않으면 User 객체 생성에 실패한다.")
        @Test
        void throwBadRequestException_whenEmailFormatIsInvalid(){
            String email = "xx@aaaa";

            CoreException result = assertThrows(CoreException.class, () -> {
                new UserModel(
                        "test1234",
                        email,
                        "1999-01-01",
                        "W"
                );
            });
            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }

        @DisplayName("생년월일이 yyyy-mm-dd 형식에 맞지 않으면, User 객체 생성에 실패한다.")
        @Test
        void throwBadRequestException_whenBirthFormatIsInvalid(){
            String birth = "19990101";
            CoreException result = assertThrows(CoreException.class, () -> {
                new UserModel(
                        "test1234",
                        "email@email.com",
                        birth,
                        "W"
                );
            });

            assertThat(result.getErrorType()).isEqualTo(ErrorType.BAD_REQUEST);
        }
    }
}
