package com.xqdd.highlightword.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {
    private T data;
    private boolean success;
    private Integer code;


    private Result(boolean success) {
        this.success = success;
    }


    private Result(boolean success, Integer code) {
        this.success = success;
        this.code = code;
    }

    private Result(boolean success, Integer code, T data) {
        this.code = code;
        this.data = data;
        this.success = success;
    }

    private Result(boolean success, T data) {
        this.data = data;
        this.success = success;
    }

    public static <T> Result<T> success(Integer code, T data) {
        return new Result<>(true, code, data);
    }

    public static Result<Object> success() {
        return new Result<>(true);
    }

    public static <T> Result<T> success(T data) {
        return new Result<>(true, data);
    }



    public static <T> ResponseEntity<Result<T>> error(Integer code, T data) {
        return ResponseEntity.status(HttpStatus.valueOf(code / 100)).body(new Result<>(false, code, data));
    }

    public static BusinessException validateException(Integer code, Object o) {
        return new BusinessException(Result.error(code, o));
    }
}
