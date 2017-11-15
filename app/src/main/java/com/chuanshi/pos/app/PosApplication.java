package com.chuanshi.pos.app;

import android.app.Application;


import com.chuanshi.pos.library.di.component.AppComponent;
import com.chuanshi.pos.library.di.component.DaggerAppComponent;
import com.chuanshi.pos.library.di.module.AppModule;
import com.chuanshi.pos.library.di.module.RetrofitModule;
import com.chuanshi.pos.utils.ApiConfig;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by zhouliancheng on 2017/10/19.
 */

public class PosApplication extends Application {

    private static PosApplication application;
    private AppComponent mAppComponent;

    public static PosApplication getInstance() {
        return application;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        application = this;
        Map<String, String> headers = new HashMap<>();
        headers.put("Accept", "application/json");
        mAppComponent = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .retrofitModule(new RetrofitModule(this, ApiConfig.BASE_URL, headers))
                .build();
    }

    public AppComponent getAppComponent() {
        return mAppComponent;
    }
}
