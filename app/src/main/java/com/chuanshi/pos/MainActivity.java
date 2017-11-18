package com.chuanshi.pos;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;

import com.chuanshi.pos.library.http.NetworkUtil;
import com.chuanshi.pos.widget.CustomWebView;

public class MainActivity extends Activity implements View.OnClickListener {

    private CustomWebView mWebView;
    private LinearLayout mNetworkErrorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mWebView = findViewById(R.id.webview);
        //此句加上也可以，防止输入法调不出来
        mWebView.requestFocusFromTouch();
        mNetworkErrorLayout = findViewById(R.id.layout_network_error);
        Button reloadBtn = findViewById(R.id.btn_reload);
        reloadBtn.setOnClickListener(this);
        loadData();
    }

    private void loadData() {
        //网络检查
        if (!NetworkUtil.isNetworkAvailable(this)) {
            mWebView.setVisibility(View.GONE);
            mNetworkErrorLayout.setVisibility(View.VISIBLE);
        } else {
            mWebView.setVisibility(View.VISIBLE);
            mNetworkErrorLayout.setVisibility(View.GONE);
            String url = "http://fwy.csshidai.com";
            mWebView.loadUrl(url);
        }
    }
    
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reload:
                loadData();
                break;
        }
    }
    
    @Override
    protected void onPause() {
        /*if (mWebView != null) {
            mWebView.onPause();
        }*/
        super.onPause();
    }

    @Override
    protected void onResume() {
        /*if (mWebView != null) {
            mWebView.onResume();
        }*/
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
