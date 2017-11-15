package com.chuanshi.pos.library.http.callback;


import com.chuanshi.pos.library.http.CommonException;

/**
 * Created by zhouliancheng on 2017/10/20.
 */

public interface NetworkCallback<T> {
    void onSuccess(T t);
    void onFailure(CommonException e);
}
