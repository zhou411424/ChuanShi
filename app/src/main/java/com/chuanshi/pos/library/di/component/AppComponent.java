package com.chuanshi.pos.library.di.component;

import android.app.Application;
import android.content.SharedPreferences;


import com.chuanshi.pos.library.di.module.AppModule;
import com.chuanshi.pos.library.di.module.RetrofitModule;
import com.chuanshi.pos.library.http.RetrofitManager;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by zhouliancheng on 2017/10/27.
 */
@Singleton
@Component(modules = {AppModule.class, RetrofitModule.class})
public interface AppComponent {
    Application getApplication();

    RetrofitManager getRetrofitManager();

    SharedPreferences getSharedPreferences();
}
