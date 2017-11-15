package com.chuanshi.pos.library.http;

/**
 * Created by zhouliancheng on 2017/10/18.
 */

public class CommonException extends Throwable {
    private int code;
    private String message;
    private Throwable throwable;

    public CommonException() {

    }

    public CommonException(String message) {
        this.message = message;
    }

    public CommonException(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public CommonException(int code, String message, Throwable throwable) {
        this.code = code;
        this.message = message;
        this.throwable = throwable;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
