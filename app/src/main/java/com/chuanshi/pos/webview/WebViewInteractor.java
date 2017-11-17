package com.chuanshi.pos.webview;

import android.app.Activity;
import android.webkit.WebView;

/**
 * Created by zhouliancheng on 2017/11/17.
 * H5和WebView交互的类
 */
public class WebViewInteractor {
    private WebView mWebView;

    public WebViewInteractor(WebView webView) {
        mWebView = webView;
    }

    /**
     * 为H5调用android提供接口
     * @param activity
     */
    public void addChuanShiJavascriptInterface(Activity activity) {
        if (mWebView != null) {
            mWebView.addJavascriptInterface(new ChuanShiJavascriptInterface(activity), "chuanshi");
        }
    }

    /**
     * H5调用Android接口类
     */
    private class ChuanShiJavascriptInterface {
        private Activity activity;

        public ChuanShiJavascriptInterface(Activity activity) {
            this.activity = activity;
        }
    }
}
