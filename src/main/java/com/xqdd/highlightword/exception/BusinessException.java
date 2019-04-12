package com.xqdd.highlightword.exception;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessException extends RuntimeException {

    protected Object response;


    public BusinessException(String message, Throwable cause, Object response) {
        super(message, cause);
        this.response = response;
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public BusinessException(String message, Object response) {
        super(message);
        this.response = response;
    }

    public BusinessException(Object response) {
        super(response.toString());
        this.response = response;
    }

    @Override
    public void printStackTrace() {
        super.printStackTrace();
        System.out.println(response);
    }
}