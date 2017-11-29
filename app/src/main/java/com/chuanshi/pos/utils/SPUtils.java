package com.chuanshi.pos.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.chuanshi.pos.app.PosApplication;

/**
 * Created by zhoulc on 17/11/29.
 */

public class SPUtils {

    private static SharedPreferences getSharedPreference() {
        return PosApplication.getInstance().getSharedPreferences(Constants.SP_NAME, Context.MODE_MULTI_PROCESS);
    }

    public static void putBoolean(String key, boolean value) {
        SharedPreferences sharedPreference = getSharedPreference();
        SharedPreferences.Editor editor = sharedPreference.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * 获取boolean的value
     *
     * @param
     * @param key      名字
     * @param defValue 默认值
     * @return
     */
    public static boolean getBoolean(String key, Boolean defValue) {
        SharedPreferences sharedPreference = getSharedPreference();
        return sharedPreference.getBoolean(key, defValue);
    }
}
