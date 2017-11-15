package com.chuanshi.pos.library.http;

import java.util.Map;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.PartMap;
import retrofit2.http.Path;
import retrofit2.http.QueryMap;
import retrofit2.http.Streaming;
import retrofit2.http.Url;

/**
 * Created by zhouliancheng on 2017/10/19.
 * 封装get，post请求方法
 */
public interface BaseApiService {

    /**
     * Rxjava2 方式的get请求
     * @param path
     * @param maps
     * @param <T>
     * @return
     */
    @GET("{path}")
    <T> Observable<T> getByRxJava(@Path("path") String path, @QueryMap Map<String, String> maps);

    /**
     * Rxjava2 方式的post请求
     * @param path
     * @param maps
     * @return
     */
    @FormUrlEncoded
    @POST("{path}")
    <T> Observable<T> postByRxJava(@Path("path") String path, @FieldMap Map<String, String> maps);

    /**
     * Retrofit2 方式的get请求
     * @param path
     * @param maps
     * @param <T>
     * @return
     */
    @GET("{path}")
    <T> Call<T> getByRetrofit(@Path("path") String path, @QueryMap Map<String, String> maps);

    /**
     * Retrofit2 方式的post请求
     * @param path
     * @param maps
     * @param <T>
     * @return
     */
    @FormUrlEncoded
    @POST("{path}")
    <T> Call<T> postByRetrofit(@Path("path") String path, @FieldMap Map<String, String> maps);

    @FormUrlEncoded
    @POST("{path}")
    Observable<ResponseBody> json(@Path("path") String path, @Body RequestBody jsonStr);

    /**
     * 上传单个文件
     * @param path
     * @param avatar
     * @return
     */
    @Multipart
    @FormUrlEncoded
    @POST("{path}")
    Observable<ResponseBody> uploadFile(@Path("path") String path, @Part("image\"; filename=\"image.jpg") RequestBody avatar);

    /**
     * 上传多个文件
     * @param path
     * @param headerMap
     * @param description
     * @param maps
     * @return
     */
    @FormUrlEncoded
    @POST("{path}")
    Observable<ResponseBody> uploadFiles(@Path("path") String path, @Path("headers") Map<String, String> headerMap,
                                         @Part("filename") String description, @PartMap() Map<String, RequestBody> maps);

    /**
     * 下载文件
     * @param filepath
     * @return
     */
    @Streaming
    @GET
    Observable<ResponseBody> downloadFile(@Url String filepath);
}
