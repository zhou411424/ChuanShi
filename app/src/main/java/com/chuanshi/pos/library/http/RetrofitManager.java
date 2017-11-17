package com.chuanshi.pos.library.http;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.chuanshi.pos.library.http.callback.NetworkCallback;
import com.chuanshi.pos.library.http.callback.RetrofitCallback;
import com.chuanshi.pos.library.http.interceptor.HeaderInterceptor;
import com.chuanshi.pos.library.http.interceptor.NetworkInterceptor;
import com.chuanshi.pos.utils.ApiConfig;
import com.jakewharton.retrofit2.adapter.rxjava2.HttpException;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import okhttp3.Cache;
import okhttp3.ConnectionPool;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by zhouliancheng on 2017/10/18.
 */

public class RetrofitManager {
    private static final String TAG = "RetrofitManager";
    private static final int DEFAULT_TIME_OUT = 5;//超时时间5s
    private static final int DEFAULT_READ_TIME_OUT = 10;
    private static final String BASE_URL = ApiConfig.BASE_URL;
    private volatile static RetrofitManager instance;
    private OkHttpClient mOkHttpClient;
    private Retrofit mRetrofit;
    private Context mContext;
    private BaseApiService mBaseApiService;
    private File httpCacheDir;
    private Cache cache;

    private RetrofitManager(Context context) {
        this(context, null, null);
    }

    private RetrofitManager(Context context, String baseUrl) {
        this(context, baseUrl, null);
    }

    private RetrofitManager(Context context, String baseUrl, Map<String, String> headerMap) {
        mContext = context;

        if (TextUtils.isEmpty(baseUrl)) {
            baseUrl = BASE_URL;
        }

        if (httpCacheDir == null) {
            httpCacheDir = new File(mContext.getCacheDir(), "http_cache");
        }

        //10M
        try {
            if (cache == null) {
                cache = new Cache(httpCacheDir, 10 * 1024 * 1024);
            }
        } catch (Exception e) {
            Log.e(TAG, "Could not create http cache", e);
        }

        //创建OkHttpClient
        mOkHttpClient = new OkHttpClient.Builder()
                .connectTimeout(DEFAULT_TIME_OUT, TimeUnit.SECONDS)
                .readTimeout(DEFAULT_READ_TIME_OUT, TimeUnit.SECONDS)
                .addNetworkInterceptor(new HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
                .addInterceptor(new HeaderInterceptor(headerMap))
                .addInterceptor(new NetworkInterceptor(context))
                .addNetworkInterceptor(new NetworkInterceptor(context))
                .cookieJar(new NovateCookieManger(context))
                .cache(cache)
                .connectionPool(new ConnectionPool(8, 15, TimeUnit.SECONDS))
                // 这里你可以根据自己的机型设置同时连接的个数和时间，我这里8个，和每个保持时间为10s
                .build();

        //创建Retrofit
        mRetrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(baseUrl)
                .build();

        mBaseApiService = create(BaseApiService.class);
    }

    public static RetrofitManager getInstance(Context context) {
        return getInstance(context, null, null);
    }

    public static RetrofitManager getInstance(Context context, String baseUrl) {
        return getInstance(context, baseUrl, null);
    }

    public static RetrofitManager getInstance(Context context, String baseUrl, Map<String, String> headerMap) {
        if (instance == null) {
            synchronized (RetrofitManager.class) {
                if (instance == null) {
                    instance = new RetrofitManager(context, baseUrl, headerMap);
                }
            }
        }
        return instance;
    }

    public Retrofit getRetrofit() {
        return mRetrofit;
    }

    public BaseApiService getBaseApiService() {
        return mBaseApiService;
    }

    public <T> T create(final Class<T> service) {
        if (mRetrofit == null) {
            try {
                // 如果报错，请调用initRetrofit初始化mRetrofit对象
                throw new CommonException("mRetrofit can't be null, please init retrofit instance");
            } catch (CommonException e) {
                e.printStackTrace();
            }
        }
        return mRetrofit.create(service);
    }

    public <T> Disposable getByRxJava(String url, Map paramMap, NetworkCallback<T> networkCallback) {
        return requestByRxJava(mBaseApiService.getByRxJava(url, paramMap), networkCallback);
    }

    public <T> Disposable postByRxJava(String url, Map<String, String> paramMap, NetworkCallback<T> networkCallback) {
        return requestByRxJava(mBaseApiService.postByRxJava(url, paramMap), (NetworkCallback<Object>) networkCallback);
    }

    public <T> void getByRetrofit(String url, Map paramMap, RetrofitCallback<T> retrofitCallback) {
        mBaseApiService.getByRetrofit(url, paramMap).enqueue(retrofitCallback);
    }

    public <T> void postByRetrofit(String url, Map paramMap, RetrofitCallback<T> retrofitCallback) {
        mBaseApiService.postByRetrofit(url, paramMap).enqueue(retrofitCallback);
    }

    /**
     * 订阅事件通过Rxjava2
     * @param observable
     * @param networkCallback
     * @param <T>
     */
    public <T> Disposable requestByRxJava(Observable<T> observable, final NetworkCallback<T> networkCallback) {
        Disposable disposable = observable.compose(RxUtil.<T>schedulersObservableTransformer()).subscribe(
                new Consumer<T>() {
                    @Override
                    public void accept(T t) throws Exception {
                        if (networkCallback != null) {
                            networkCallback.onSuccess(t);
                        }
                    }
                }, new Consumer<Throwable>() {

                    @Override
                    public void accept(Throwable throwable) throws Exception {
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
                });

        return disposable;
    }
}
