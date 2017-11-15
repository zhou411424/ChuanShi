package com.chuanshi.pos.library.http;

/**
 * Created by zhouliancheng on 2017/10/19.
 * 网络返回基类 支持泛型
 */
public class BaseResponse<T> {
    private int code;
    private String msg;
    private T data;

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    public T getData() {
        return data;
    }
}
