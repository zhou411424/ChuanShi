package com.chuanshi.pos.library.mvp;

import android.app.Activity;
import android.util.Log;


import com.chuanshi.pos.library.rxbus2.RxBus;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

/**
 * Created by zhouliancheng on 2017/10/17.
 */
public class BasePresenter<M extends BaseContract.IModel, V extends BaseContract.IView> implements BaseContract.IPresenter {
    private static final String TAG = "BasePresenter";
    private boolean mEnableRxBus = true;
    protected BaseContract.IModel mModel;
    protected BaseContract.IView mView;
    protected CompositeDisposable mCompositeDisposable;

    /**
     * 如果当前页面同时需要 Model 层和 View 层,则使用此构造函数(默认)
     * @param m
     * @param v
     */
    public BasePresenter(M m, V v) {
        this.mModel = m;
        this.mView = v;
        onStart();
    }

    /**
     * 如果当前页面不需要操作数据,只需要 View 层,则使用此构造函数
     * @param v
     */
    public BasePresenter(V v) {
        this.mView = v;
        onStart();
    }

    public BasePresenter() {
        onStart();
    }

    /**
     * 是否使用Rxbus（基于Rxjava2）
     * @return
     */
    public boolean useRxBus() {
        return mEnableRxBus;
    }

    /**
     * 将所有正在处理的Subscription都添加到CompositeSubscription中。统一退出的时候注销观察
     * @param disposable
     */
    public void addDisposable(Disposable disposable) {
        if (mCompositeDisposable == null || mCompositeDisposable.isDisposed()) {
            mCompositeDisposable = new CompositeDisposable();
        }
        mCompositeDisposable.add(disposable);
    }

    /**
     * 在界面退出等需要解绑观察者的情况下调用此方法统一解绑，防止Rx造成的内存泄漏
     */
    public void dispose() {
        if (mCompositeDisposable != null && !mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
        }
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart==>");
        if (useRxBus()) {
            RxBus.getDefault().register(this);
        }
    }

    /**
     * 在框架中 {@link Activity#onDestroy()} 会默认调用{@link BaseContract.IPresenter#onDestroy()}
     */
    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy==>");
        if (useRxBus()) {
            RxBus.getDefault().unregister(this);
        }
        dispose();
        if (mModel != null) {
            mModel.onDestroy();
        }
        mModel = null;
        mView = null;
        mCompositeDisposable = null;
    }
}
