package com.chuanshi.pos;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;

import com.chuanshi.pos.widget.CustomWebView;

public class MainActivity extends Activity {

    private CustomWebView mWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mWebView = (CustomWebView) findViewById(R.id.webview);
        //此句加上也可以，防止输入法调不出来
        mWebView.requestFocusFromTouch();

        String url = "http://fwy.csshidai.com";
        mWebView.loadUrl(url);
    }

    @Override
    protected void onPause() {
        if (mWebView != null) {
            mWebView.onPause();
        }
        super.onPause();
    }

    @Override
    protected void onResume() {
        if (mWebView != null) {
            mWebView.onResume();
        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView.destroy();
        }
        super.onDestroy();
    }
}
