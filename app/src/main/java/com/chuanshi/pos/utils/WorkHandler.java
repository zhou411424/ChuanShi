package com.chuanshi.pos.utils;

import android.os.Handler;
import android.os.HandlerThread;

/**
 * Created by zhouliancheng on 2017/12/9.
 */

public class WorkHandler {
    private static Handler workHandler;

    public static Handler getWorkHandler() {
        if (workHandler == null) {
            synchronized (WorkHandler.class) {
                if (workHandler == null) {
                    HandlerThread handlerThread = new HandlerThread("WorkHandler");
                    handlerThread.start();
                    workHandler = new Handler(handlerThread.getLooper());
                }
            }
        }
        return workHandler;
    }

    /***
     * 非UI线程调用耗时操作
     *
     * @param r
     */
    public static void post2work(Runnable r) {
        getWorkHandler().post(r);
    }

    public static void post2workDelay(Runnable r, long time) {
        getWorkHandler().postDelayed(r, time);
    }

    public static void removeRunnale(Runnable r) {
        if (null != r) {
            getWorkHandler().removeCallbacks(r);
        }
    }

    public static void removeCallbacksAndMessages() {
        getWorkHandler().removeCallbacksAndMessages(null);
    }

}
