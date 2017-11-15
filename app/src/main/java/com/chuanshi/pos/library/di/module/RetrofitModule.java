package com.chuanshi.pos.library.di.module;

import android.content.Context;


import com.chuanshi.pos.library.http.RetrofitManager;

import java.util.Map;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by zhouliancheng on 2017/10/25.
 */
@Module
public class RetrofitModule {
    private Context context;
    private String baseUrl;
    private Map<String, String> headers;

    public RetrofitModule(Context context, String baseUrl, Map<String, String> headers) {
        this.context = context;
        this.baseUrl = baseUrl;
        this.headers = headers;
    }

    /**
     * 当一个Module里多个不同的方法返回同一个类型的实例对象时，用@Named标记进行区分，如@Named("param_1")
     * @return
     */
    @Singleton
    @Provides
    public RetrofitManager provideRetrofitManager() {
        return RetrofitManager.getInstance(context, baseUrl, headers);
    }

}
