package com.example.want.common;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@NoArgsConstructor
public class CommonResDto {
    private int status_code;
    private String status_message;
    private Object result;

    public CommonResDto(HttpStatus httpStatus, String statusMessage, Object data){
        this.status_code = httpStatus.value();
        this.status_message = statusMessage;
        this.result = data;
    }
}
