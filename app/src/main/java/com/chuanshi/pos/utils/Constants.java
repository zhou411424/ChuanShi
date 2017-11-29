package com.chuanshi.pos.utils;

import retrofit2.http.PUT;

/**
 * Created by zhoulc on 17/11/25.
 */

public class Constants {
    //支付请求：requestCode=1, 查询交易详情 requestCode = 2
    public static final int REQUEST_CODE_PAYMENT = 1;
    public static final int REQUEST_CODE_QUERY_BILL_DETAIL = 2;

    //定义支付成功响应码，成功：1，失败：0
    public static final String RESPONSE_CODE_SUCCESS = "1";
    public static final String RESPONSE_CODE_FAIL = "0";

    //sharedpreferences
    public static final String SP_NAME = "pos_preferences";
    public static final String IS_FIRST_RUN = "isFirstRun";
}
