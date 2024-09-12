package com.example.want.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class CommonErrorDto {
    private int status_code;
    private String status_message;

    public CommonErrorDto(HttpStatus status_code, String status_message){
        this.status_code = status_code.value();
        this.status_message = status_message;
    }
}
