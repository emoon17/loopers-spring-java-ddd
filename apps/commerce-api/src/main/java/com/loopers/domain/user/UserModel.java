package com.loopers.domain.user;

import com.loopers.domain.BaseEntity;
import com.loopers.support.error.CoreException;
import com.loopers.support.error.ErrorType;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

@Entity
@Slf4j
@Getter
@Table(name = "users")
public class UserModel extends BaseEntity {

    private String loginId;
    private String email;
    private String birth;
    private String gender; // W : women / M : Men

    protected UserModel() {}

    public UserModel(final String loginId, final String email, final String birth, final String gender) {
        if(loginId == null || !loginId.matches("^[a-zA-Z0-9]{1,10}$")){
            throw new CoreException(ErrorType.BAD_REQUEST, "ID는 영문 및 숫자 10자 이내여야 합니다.");
        }
        if(email == null || !email.matches("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$")){
            throw new CoreException(ErrorType.BAD_REQUEST, "이메일 형식이 올바르지 않습니다.");
        }
        if(gender == null || !(gender.equals("M") || gender.equals("W"))) {
            throw new CoreException(ErrorType.BAD_REQUEST, "성별은 M 또는 W 입력 가능합니다.");
        }

        this.loginId = loginId;
        this.email = email;
        this.gender = gender;

        try {
            this.birth = LocalDate.parse(birth).toString();
        } catch(DateTimeParseException e){
            throw new CoreException(ErrorType.BAD_REQUEST, "생년월일은 yyyy-mm-dd 형식이어야 합니다.");
        }


    }
}
