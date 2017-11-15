package com.chuanshi.pos.library.http.interceptor;

import java.io.IOException;
import java.util.Map;

import okhttp3.Headers;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by zhouliancheng on 2017/10/18.
 * 添加公共header参数拦截器
 */
public class HeaderInterceptor implements Interceptor {
    private final Map<String, String> headerMap;

    public HeaderInterceptor(Map<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request().newBuilder().headers(Headers.of(headerMap)).build();
        return chain.proceed(request);
    }
}
