package com.chuanshi.pos.utils;

import android.os.Handler;
import android.os.Looper;


/**
 * Created by zhouliancheng on 2017/12/9.
 */

public class UIHandler {
    private static Handler mainHandler = new Handler(Looper.getMainLooper());
    public static Handler getMainHandler() {
        return mainHandler;
    }

    /***
     * Runnable post到主线程
     *
     * @param r
     */
    public static void post2UI(Runnable r) {
        getMainHandler().post(r);
    }

    public static void postDelay2UI(Runnable r, long time) {
        getMainHandler().postDelayed(r, time);
    }

    public static void removeRunnale(Runnable r) {
        if (null != r) {
            getMainHandler().removeCallbacks(r);
        }
    }

    public static void removeCallbacksAndMessages() {
        getMainHandler().removeCallbacksAndMessages(null);
    }
}
