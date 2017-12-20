package com.chuanshi.pos.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhoulc on 17/12/18.
 * json解析
 */

public class GsonUtils {

    private static Gson gson = null;

    static {
        if (gson == null) {
            gson = new Gson();
        }
    }

    private GsonUtils() {
    }

    /**
     * 转成bean
     *
     * @param jsonString
     * @param cls
     * @return
     */
    public static <T> T fromJson(String jsonString, Class<T> cls) {
        T t = null;
        if (gson != null) {
            t = gson.fromJson(jsonString, cls);
        }
        return t;
    }

    /**
     * 转成list
     * 泛型在编译期类型被擦除导致报错
     *
     * @param jsonString
     * @param cls
     * @return
     */
    public static <T> List<T> fromJsonArray(String jsonString,String key, Class<T> cls) {
        Gson gson = new Gson();
        List<T> list = new ArrayList<T>();
        JsonObject jsonObject = new
                JsonParser().parse(jsonString).getAsJsonObject();
        JsonArray array = jsonObject.getAsJsonArray(key);
        for(final JsonElement elem : array){
            list.add(gson.fromJson(elem, cls));
        }
        return list;
    }

    /**
     * 转成list
     * 泛型在编译期类型被擦除导致报错
     *
     * @param jsonString
     * @param cls
     * @return
     */
    public static <T> List<T> fromJsonArray(String jsonString, Class<T> cls) {
        Gson gson = new Gson();
        List<T> list = new ArrayList<T>();
        JsonArray array= new
                JsonParser().parse(jsonString).getAsJsonArray();
        for(final JsonElement elem : array){
            list.add(gson.fromJson(elem, cls));
        }
        return list;
    }


}
