package com.chuanshi.pos;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chuanshi.pos.library.http.NetworkUtil;
import com.chuanshi.pos.webview.CustomWebChromeClient;
import com.chuanshi.pos.webview.CustomWebViewClient;
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

        mWebView.setWebChromeClient(new CustomWebChromeClient(this));
        mWebView.setWebViewClient(new CustomWebViewClient());
        //为H5调用android提供接口
        mWebView.addJavascriptInterface(new ChuanShiJavascriptInterface(), "chuanshi");

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

    /**
     * H5调用Android接口类
     */
    private class ChuanShiJavascriptInterface {

        public ChuanShiJavascriptInterface() {
        }

        /**
         * 开始收单
         */
        @JavascriptInterface
        public void startPayment(String orderId, String orderName) {
            Toast.makeText(MainActivity.this, orderId + "==>"+orderName, Toast.LENGTH_LONG).show();
            //'" + orderId+ "', '" + orderName+"'
            mWebView.loadUrl("javascript:androidCallH5();");
//            MainActivity.this.startAcquiring();
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

    /**
     * 调用者启用收单支付应用支付功能组件
     */
    private void startAcquiring() {
        try {
            ComponentName component = new ComponentName("com.newland.caishen", "com.newland.caishen.ui.activity.MainActivity");
            Intent intent = new Intent();
            intent.setComponent(component);

            Bundle bundle = new Bundle();
            bundle.putString("msg_tp", "0200");
            bundle.putString("pay_tp",  "0");
            bundle.putString("proc_tp",  "00");
            bundle.putString("proc_cd", "000000");
            bundle.putString("amt",     "100.01");
            bundle.putString("order_no",     "xxxxxxxxxxx");
            bundle.putString("appid",     "com.nld.trafficmanage");
            bundle.putString("time_stamp", "20150930035201");
            bundle.putString("print_info", "订单商品明细单价等xxxxxx");
            intent.putExtras(bundle);
            this.startActivityForResult(intent, 1);
        } catch(ActivityNotFoundException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Bundle bundle = data.getExtras();
        if (requestCode == 1&&bundle != null) {
            switch (resultCode) {
                // 支付成功
                case Activity.RESULT_OK:
                    String msgTp = bundle.getString("msg_tp");
                    if (TextUtils.equals(msgTp, "0210")) {
                        // TODO:
                    }
                    break;
                // 支付取消
                case Activity.RESULT_CANCELED:
                    String reason = bundle.getString("reason");
                    if (reason != null) {
                        // TODO:
                    }
                    break;

                default:
                    // TODO:
                    break;
            }
        }

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
