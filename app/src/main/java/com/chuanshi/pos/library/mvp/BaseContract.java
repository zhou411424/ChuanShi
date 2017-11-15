package com.chuanshi.pos.library.mvp;

import android.app.Activity;

/**
 * Created by zhouliancheng on 2017/10/17.
 */
public interface BaseContract {

    /**
     * 框架要求框架中的每个 Presenter 都需要实现此类,以满足规范
     */
    interface IPresenter {
        /**
         * 做一些初始化操作
         */
        void onStart();

        /**
         * 在框架中 {@link Activity#onDestroy()} 会默认调用{@link IPresenter#onDestroy()}
         */
        void onDestroy();
    }

    /**
     * 框架要求框架中的每个 View 都需要实现此类,以满足规范
     */
    interface IView {
        /**
         * 显示加载
         */
        void showLoading();

        /**
         * 隐藏加载
         */
        void hideLoading();

        /**
         * 显示toast消息
         * @param message
         */
        void showToast(String message);

    }

    /**
     * 框架要求框架中的每个 Model 都需要实现此类,以满足规范
     */
    interface IModel {
        /**
         * 在框架中 {@link BasePresenter#onDestroy()} 会默认调用{@link IModel#onDestroy()}
         */
        void onDestroy();
    }
}
