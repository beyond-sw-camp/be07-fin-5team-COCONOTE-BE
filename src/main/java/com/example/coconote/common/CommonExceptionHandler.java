package com.example.want.common;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.persistence.EntityNotFoundException;

// @ControllerAdvice : 모든 컨트롤러에 적용되는 예외처리 클래스
@ControllerAdvice
public class CommonExceptionHandler {

//    Controller단에서 발생하는 모든 EntityNotFoundException을 catch
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<CommonErrorDto> entityNotFound(EntityNotFoundException e){
        CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonErrorDto> illegalArgument(IllegalArgumentException e){
        CommonErrorDto commonErrorDto = new CommonErrorDto(HttpStatus.BAD_REQUEST, e.getMessage());
        return new ResponseEntity<>(commonErrorDto, HttpStatus.BAD_REQUEST);
    }

}
