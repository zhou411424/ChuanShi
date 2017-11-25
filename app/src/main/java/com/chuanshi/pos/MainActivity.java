package com.chuanshi.pos;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chuanshi.pos.library.http.NetworkUtil;
import com.chuanshi.pos.utils.NotProguard;
import com.chuanshi.pos.webview.CustomWebChromeClient;
import com.chuanshi.pos.webview.CustomWebViewClient;
import com.chuanshi.pos.widget.CustomWebView;

import static android.content.ContentValues.TAG;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
//    private WebView mWebView;
    private CustomWebView mWebView;
    private LinearLayout mNetworkErrorLayout;
    private EditText et_pay_tp;
    private String proc_cd = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //全屏
        getWindow().setFlags(WindowManager.LayoutParams. FLAG_FULLSCREEN ,
                WindowManager.LayoutParams. FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);

        mWebView = findViewById(R.id.webview);
        //此句加上也可以，防止输入法调不出来
//        mWebView.requestFocusFromTouch();

        mWebView.setWebChromeClient(new CustomWebChromeClient(this));
        mWebView.setWebViewClient(new CustomWebViewClient());
        //为H5调用android提供接口
        mWebView.addJavascriptInterface(new ChuanShiJavascriptInterface(), "chuanshi");

        mNetworkErrorLayout = findViewById(R.id.layout_network_error);
        Button reloadBtn = findViewById(R.id.btn_reload);
        reloadBtn.setOnClickListener(this);

        /*Button btn_1 = findViewById(R.id.btn_1);
        Button btn_2 = findViewById(R.id.btn_2);
        Button btn_3 = findViewById(R.id.btn_3);
        Button btn_4 = findViewById(R.id.btn_4);
        Button btn_5 = findViewById(R.id.btn_5);
        Button btn_6 = findViewById(R.id.btn_6);
        Button btn_7 = findViewById(R.id.btn_7);
        Button btn_8 = findViewById(R.id.btn_8);
        Button btn_9 = findViewById(R.id.btn_9);
        Button btn_10 = findViewById(R.id.btn_10);

        et_pay_tp = findViewById(R.id.et_pay_tp);
        btn_1.setOnClickListener(this);
        btn_2.setOnClickListener(this);
        btn_3.setOnClickListener(this);
        btn_4.setOnClickListener(this);
        btn_5.setOnClickListener(this);
        btn_6.setOnClickListener(this);
        btn_7.setOnClickListener(this);
        btn_8.setOnClickListener(this);
        btn_9.setOnClickListener(this);
        btn_10.setOnClickListener(this);*/


//        Button btnCallH5 = findViewById(R.id.btn_call_h5);
//        btnCallH5.setOnClickListener(this);
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
//            String url = "http://fwy.csshidai.com";
//            String url = "file:///android_asset/androidcallh5.html";
            String url = "http://www.chuanshitech.com";
            mWebView.loadUrl(url);
        }
    }

    /**
     * H5调用Android接口类
     */
    private class ChuanShiJavascriptInterface {

        /**
         * 开始收单
         */
        //@NotProguard
        @JavascriptInterface
        public void startPayment(String msg_tp, String pay_tp, String proc_tp,
                                 String proc_cd, String systraceno, String amt,
                                 String order_no, String batchbillno, String appid,
                                 String time_stamp, String print_info) {
//            Toast.makeText(MainActivity.this, orderId + "==>"+orderName, Toast.LENGTH_LONG).show();
//            mWebView.loadUrl("javascript:androidCallH5()");
            MainActivity.this.startPayment(msg_tp, pay_tp, proc_tp, proc_cd,
                    systraceno, amt, order_no, batchbillno, appid, time_stamp, print_info);
        }
    }

    @Override
    public void onClick(View view) {
//        String pay_tp=et_pay_tp.getText().toString();
        switch (view.getId()) {
            case R.id.btn_reload:
                loadData();
                break;
            /*ase R.id.btn_call_h5:
                androidCallH5();
                break;
            case R.id.btn_1:
                proc_cd = "000000";
                MainActivity.this.startPayment(pay_tp, proc_cd);
                break;
            case R.id.btn_2:
                proc_cd = "200000";
                MainActivity.this.startPayment(pay_tp, proc_cd);
                break;
            case R.id.btn_3:
                proc_cd = "300000";
                MainActivity.this.startPayment(pay_tp, proc_cd);
                break;
            case R.id.btn_4:
                proc_cd = "330000";
                MainActivity.this.startPayment(pay_tp, proc_cd);
                break;
            case R.id.btn_5:
                proc_cd = "400000";
                MainActivity.this.startPayment(pay_tp, proc_cd);
                break;
            case R.id.btn_6:
                proc_cd = "440000";
                MainActivity.this.startPayment(pay_tp, proc_cd);
                break;
            case R.id.btn_7:
                proc_cd = "660000";
                MainActivity.this.startPayment(pay_tp, proc_cd);
                break;
            case R.id.btn_8:
                proc_cd = "680000";
                MainActivity.this.startPayment(pay_tp, proc_cd);
                break;
            case R.id.btn_9:
                proc_cd = "700000";
                MainActivity.this.startPayment(pay_tp, proc_cd);
                break;
            case R.id.btn_10:
                proc_cd = "900000";
                MainActivity.this.startPayment(pay_tp, proc_cd);
                break;*/
        }
    }

    private void androidCallH5() {
        String orderId = "212222";
        mWebView.loadUrl("javascript:androidCallH5('" + orderId+ "');");

        String orderName = "ssssss";
//        mWebView.loadUrl("javascript:androidCallH5('" + orderId+ "', '" + orderName+"');");

    }

    /**
     * android支付结果回调给h5
     * @param responseCode
     * @param order_no
     * @param amt
     * @param txndetail
     */
    private void paymentCallback(String responseCode, String order_no, String amt, String txndetail) {
        if (txndetail == null) {
            txndetail = "";
        }
        if (mWebView != null) {
            mWebView.loadUrl("javascript:paymentCallback('" + responseCode+ "', '" + order_no+ "', '" + amt+"', '" + txndetail+"');");
        }
    }

    /**
     * 调用者启用收单支付应用支付功能组件
     */
    private void startPayment(String msg_tp, String pay_tp, String proc_tp,
                                String proc_cd, String systraceno, String amt,
                                String order_no, String batchbillno, String appid,
                                String time_stamp, String print_info) {
        try {
            ComponentName component = new ComponentName("com.newland.caishen", "com.newland.caishen.ui.activity.MainActivity");
            Intent intent = new Intent();
            intent.setComponent(component);

            Bundle bundle = new Bundle();
            bundle.putString("msg_tp", msg_tp);
            bundle.putString("pay_tp",  pay_tp);
            bundle.putString("proc_tp",  proc_tp);
            bundle.putString("proc_cd", proc_cd);
            bundle.putString("systraceno", systraceno);
            bundle.putString("amt", amt);
            bundle.putString("order_no", order_no);
            bundle.putString("batchbillno", batchbillno);
            bundle.putString("appid", appid);//com.nld.trafficmanage
            bundle.putString("time_stamp", time_stamp);
            bundle.putString("print_info", print_info);
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
        Log.d(TAG, "onActivityResult==>bundle="+(bundle == null)
                +", requestCode="+requestCode+", resultCode="+resultCode);
        if (requestCode == 1 && bundle != null) {
            String msgTp = bundle.getString("msg_tp");
            String order_no = bundle.getString("order_no");
            String amt = bundle.getString("amt");
            String txndetail = bundle.getString("txndetail");
            switch (resultCode) {
                // 支付成功
                case Activity.RESULT_OK:
                    if (TextUtils.equals(msgTp, "0210")) {
                        Log.d(TAG, "pay success==>order_no="+order_no
                                +", amt="+amt
                                +", txndetail="+txndetail);
                        Toast.makeText(MainActivity.this, "支付成功", Toast.LENGTH_LONG).show();
                        MainActivity.this.paymentCallback("1", order_no, amt, txndetail);
                    }
                    break;
                // 支付取消
                case Activity.RESULT_CANCELED:
                    String reason = bundle.getString("reason");
                    if (reason != null) {
                        Log.d(TAG, "pay fail==>order_no="+order_no
                                +", amt="+amt
                                +", txndetail="+txndetail);
                        Toast.makeText(MainActivity.this, "支付取消", Toast.LENGTH_LONG).show();
                        MainActivity.this.paymentCallback("0", order_no, amt, reason);
                    }
                    break;

                default:
                    // TODO:
                    Log.d(TAG, "pay unknown");
                    break;
            }
        }

    }

    @Override
    protected void onPause() {
//        if (mWebView != null) {
//            mWebView.onPause();
//        }
        super.onPause();
    }

    @Override
    protected void onResume() {
//        if (mWebView != null) {
//            mWebView.onResume();
//        }
        super.onResume();
    }

    @Override
    protected void onDestroy() {
//        if (mWebView != null) {
//            mWebView.removeAllViews();
//            mWebView.destroy();
//        }
        super.onDestroy();
    }
    
}
