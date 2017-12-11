package com.chuanshi.pos;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chuanshi.pos.library.http.NetworkUtil;
import com.chuanshi.pos.utils.Constants;
import com.chuanshi.pos.utils.SoundPlayer;
import com.chuanshi.pos.utils.WorkHandler;
import com.chuanshi.pos.webview.CustomWebChromeClient;
import com.chuanshi.pos.webview.CustomWebViewClient;
import com.chuanshi.pos.widget.CustomWebView;

public class MainActivity extends Activity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private CustomWebView mWebView;
    private LinearLayout mNetworkErrorLayout;
    private ImageView mWelcomeIv;
    private boolean mFirstRun = true;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };
    private Button mSoundPlayBtn;
    private SoundPlayer mSoundPlayer;
    private Button mSoundStopBtn;

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

        mWelcomeIv = findViewById(R.id.iv_welcome_img);
        if (mFirstRun) {
            mFirstRun = false;
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mWelcomeIv.setVisibility(View.GONE);
                }
            }, 2000);
        }

        mSoundPlayBtn = findViewById(R.id.btn_sound_play);
        mSoundStopBtn = findViewById(R.id.btn_sound_stop);
        mSoundPlayBtn.setOnClickListener(this);
        mSoundStopBtn.setOnClickListener(this);
        //播放语音
        mSoundPlayer = new SoundPlayer(this);

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
            String url = "http://www.csshidai.com";
//            String url = "file:///android_asset/androidcallh5.html";
//            String url = "http://www.chuanshitech.com";
            mWebView.loadUrl(url);
        }
    }

    /**
     * H5调用Android接口类
     */
    private class ChuanShiJavascriptInterface {

        /**
         * 开始收单（交易接口）
         */
        //@NotProguard
        @JavascriptInterface
        public void startPayment(String msg_tp, String pay_tp, String proc_tp,
                                 String proc_cd, String systraceno, String amt,
                                 String order_no, String batchbillno, String appid,
                                 String time_stamp, String print_info) {
            MainActivity.this.startPayment(msg_tp, pay_tp, proc_tp, proc_cd,
                    systraceno, amt, order_no, batchbillno, appid, time_stamp, print_info);
        }

        /**
         * 查询交易详情（查询接口）
         * @param msg_tp
         * @param pay_tp
         * @param order_no
         * @param batchbillno
         * @param appid
         */
        @JavascriptInterface
        public void queryBillDetail(String msg_tp, String pay_tp, String order_no,
                                    String batchbillno, String appid) {
            MainActivity.this.queryBillDetail(msg_tp, pay_tp, order_no,
                    batchbillno, appid);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reload:
                loadData();
                break;
            case R.id.btn_sound_play:
                playSound();
                break;
            case R.id.btn_sound_stop:
                stopSound();
                break;
        }
    }

    private void playSound() {
        WorkHandler.post2work(mPlayGiftSoundRunnable);
    }

    private void stopSound() {
        WorkHandler.post2work(mStopGiftSoundRunnable);
    }

    private Runnable mStopGiftSoundRunnable = new Runnable() {
        @Override
        public void run() {
            mSoundPlayer.stopSound();
        }
    };

    private Runnable mPlayGiftSoundRunnable = new Runnable() {

        @Override
        public void run() {
            if (mSoundPlayer != null) {
                mSoundPlayer.playSound(R.raw.new_order_notice);
            }
        }
    };

    /**
     * android支付结果回调给h5
     * @param responseCode
     * @param order_no
     * @param amt
     * @param txndetail
     */
    private void paymentCallback(String responseCode, String pay_tp, String order_no, String amt, String txndetail) {
        if (txndetail == null) {
            txndetail = "";
        }
        if (mWebView != null) {
            mWebView.loadUrl("javascript:paymentCallback('" + responseCode+ "', '" + pay_tp+ "'," +
                    " '" + order_no+ "', '" + amt+"', '" + txndetail+"');");
        }
    }

    /**
     * android查询交易详情后回调给h5
     * @param responseCode
     * @param order_no
     * @param txndetail
     */
    private void queryBillDetailCallback(String responseCode, String order_no, String txndetail) {
        if (txndetail == null) {
            txndetail = "";
        }
        if (mWebView != null) {
            mWebView.loadUrl("javascript:queryBillDetailCallback('" + responseCode+ "', '" + order_no+ "', '" + txndetail+"');");
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
            if (!"".equals(systraceno)) {
                bundle.putString("systraceno", systraceno);
            }
            if (!"".equals(amt)) {
                bundle.putString("amt", amt);
            }
            bundle.putString("order_no", order_no);
            if (!"".equals(batchbillno)) {
                bundle.putString("batchbillno", batchbillno);
            }
            bundle.putString("appid", appid);//com.nld.trafficmanage
            bundle.putString("time_stamp", time_stamp);
            if (!"".equals(print_info)) {
                bundle.putString("print_info", print_info);
            }
            intent.putExtras(bundle);

            this.startActivityForResult(intent, Constants.REQUEST_CODE_PAYMENT);
        } catch(ActivityNotFoundException e) {
            e.printStackTrace();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 查询交易详情
     * @param msg_tp
     * @param pay_tp
     * @param order_no
     * @param batchbillno
     * @param appid
     */
    private void queryBillDetail(String msg_tp, String pay_tp, String order_no,
                                 String batchbillno, String appid) {
        try {
            ComponentName component = new ComponentName("com.newland.caishen", "com.newland.caishen.ui.activity.MainActivity");
            Intent intent = new Intent();
            intent.setComponent(component);

            Bundle bundle = new Bundle();
            bundle.putString("msg_tp", msg_tp);
            bundle.putString("pay_tp",  pay_tp);
            bundle.putString("order_no", order_no);
            if (!"".equals(batchbillno)) {
                bundle.putString("batchbillno", batchbillno);
            }
            bundle.putString("appid", appid);//com.nld.trafficmanage
            intent.putExtras(bundle);

            this.startActivityForResult(intent, Constants.REQUEST_CODE_QUERY_BILL_DETAIL);
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
//        Log.d(TAG, "onActivityResult==>bundle="+(bundle == null)
//                +", requestCode="+requestCode+", resultCode="+resultCode);
        if (bundle != null) {
            if (requestCode == Constants.REQUEST_CODE_PAYMENT) {
                String msgTp = bundle.getString("msg_tp");
                String pay_tp = bundle.getString("pay_tp");
                String order_no = bundle.getString("order_no");
                String amt = bundle.getString("amt");
                String txndetail = bundle.getString("txndetail");
                switch (resultCode) {
                    // 支付成功
                    case Activity.RESULT_OK:
                        if (TextUtils.equals(msgTp, "0210")) {
//                            Log.d(TAG, "pay success==>order_no="+order_no
//                                    +", pay_tp="+pay_tp
//                                    +", amt="+amt
//                                    +", txndetail="+txndetail);
                            Toast.makeText(MainActivity.this, "支付成功", Toast.LENGTH_LONG).show();
                            MainActivity.this.paymentCallback(Constants.RESPONSE_CODE_SUCCESS,
                                    pay_tp, order_no, amt, txndetail);
                        }
                        break;
                    // 支付取消
                    case Activity.RESULT_CANCELED:
                        String reason = bundle.getString("reason");
                        if (reason != null) {
//                            Log.d(TAG, "pay fail==>order_no="+order_no
//                                    +", pay_tp="+pay_tp
//                                    +", amt="+amt
//                                    +", txndetail="+txndetail);
                            Toast.makeText(MainActivity.this, "支付取消", Toast.LENGTH_LONG).show();
                            MainActivity.this.paymentCallback(Constants.RESPONSE_CODE_FAIL,
                                    pay_tp, order_no, amt, reason);
                        }
                        break;

                    default:
                        // TODO:
                        Log.d(TAG, "pay unknown");
                        break;
                }
            } else if (requestCode == Constants.REQUEST_CODE_QUERY_BILL_DETAIL) {
                String msgTp = bundle.getString("msg_tp");
                String order_no = bundle.getString("order_no");
                String txndetail = bundle.getString("txndetail");
                switch (resultCode) {
                    // 查询成功
                    case Activity.RESULT_OK:
                        if (TextUtils.equals(msgTp, "0310")) {
//                            Log.d(TAG, "query success==>order_no="+order_no
//                                    +", txndetail="+txndetail);
//                            Toast.makeText(MainActivity.this, "查询成功", Toast.LENGTH_LONG).show();
                            MainActivity.this.queryBillDetailCallback(Constants.RESPONSE_CODE_SUCCESS, order_no, txndetail);
                        }
                        break;
                    // 查询取消
                    case Activity.RESULT_CANCELED:
                        String reason = bundle.getString("reason");
                        if (reason != null) {
//                            Log.d(TAG, "query fail==>order_no="+order_no
//                                    +", txndetail="+txndetail);
//                            Toast.makeText(MainActivity.this, "查询取消", Toast.LENGTH_LONG).show();
                            MainActivity.this.queryBillDetailCallback(Constants.RESPONSE_CODE_FAIL, order_no, reason);
                        }
                        break;

                    default:
                        // TODO:
                        Log.d(TAG, "query unknown");
                        break;
                }
            }
        }


    }

    @Override
    protected void onPause() {
//        if (mWebView != null) {
//            mWebView.onPause();
//        }
//        mWebView.pauseTimers();
//        if(isFinishing()){
//            mWebView.loadUrl("about:blank");
//            setContentView(new FrameLayout(this));
//        }
        super.onPause();
    }

    @Override
    protected void onResume() {
//        if (mWebView != null) {
//            mWebView.onResume();
//        }
//        mWebView.resumeTimers();
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView.destroy();
        }
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
        // clear sound
        if (mSoundPlayer != null) {
            mSoundPlayer.release();
            mSoundPlayer = null;
        }
        WorkHandler.removeRunnale(mPlayGiftSoundRunnable);
        WorkHandler.removeRunnale(mStopGiftSoundRunnable);
        super.onDestroy();
    }
    
}
