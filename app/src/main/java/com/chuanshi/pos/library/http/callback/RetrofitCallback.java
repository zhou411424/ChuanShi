package com.chuanshi.pos.library.http.callback;

import com.chuanshi.pos.library.http.CommonException;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.net.ssl.SSLHandshakeException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by zhouliancheng on 2017/10/20.
 * retrofit 的请求方式回调
 */
public class RetrofitCallback<T> implements Callback<T> {
    private NetworkCallback<T> networkCallback;

    public RetrofitCallback(NetworkCallback<T> networkCallback) {
        this.networkCallback = networkCallback;
    }

    @Override
    public void onResponse(Call<T> call, Response<T> response) {
        if (networkCallback != null && response != null && response.isSuccessful()) {
            networkCallback.onSuccess(response.body());
        }
    }

    @Override
    public void onFailure(Call<T> call, Throwable throwable) {
        if (networkCallback != null) {
            if (throwable instanceof SocketException) {
                networkCallback.onFailure(new CommonException("连接异常,请检查网络连接-10002"));
            } else if (throwable instanceof SSLHandshakeException) {
                networkCallback.onFailure(new CommonException("连接异常-10001"));
            } else if (throwable instanceof SocketTimeoutException) {
                networkCallback.onFailure(new CommonException("请求超时,请稍后重试-10003"));
            } else if (throwable instanceof IOException) {
                networkCallback.onFailure(new CommonException("网络异常,请检查网络连接-10000"));
            } else {
                if (throwable instanceof HttpException) {
                    int code = ((HttpException) throwable).response().code();
                    try {
                        ResponseBody responseBody = ((HttpException) throwable).response().errorBody();
                        networkCallback.onFailure(new CommonException(code, "网络服务异常:code:" + code + ";body:" + responseBody.string()));
                    } catch (IOException var4) {
                        networkCallback.onFailure(new CommonException(code, "解析错误"));
                    }
                } else {
                    networkCallback.onFailure(new CommonException("未知错误"));
                }
            }
        }
    }
}
