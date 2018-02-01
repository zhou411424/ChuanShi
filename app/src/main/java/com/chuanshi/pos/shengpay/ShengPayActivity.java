package com.chuanshi.pos.shengpay;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chuanshi.pos.R;

import static android.R.attr.mode;


/**
 * Created by zhouliancheng on 2018/1/30.
 */

public class ShengPayActivity extends Activity {
    private static final String TAG = "ShengPayActivity";
    private RFReceiver rfReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheng_pay);
        Button mShengPayBtn = findViewById(R.id.btn_sheng_pay);

        initReceiver();

        mShengPayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShengPay("0", "000000000001");
            }
        });
    }

    private void initReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.shengpaysdk.rf.searchcard");
        rfReceiver = new RFReceiver();
        registerReceiver(rfReceiver, intentFilter);
    }

    /**
     * 普通交易
     */
    private void onShengPay(String transName, String amount) {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.shengpay.smartpos.shengpaysdk","com.shengpay.smartpos.shengpaysdk.activity.MainActivity"));
        intent.putExtra("appId", getPackageName());
        intent.putExtra("transName", transName);
        intent.putExtra("amount", amount);
        startActivityForResult(intent, 0);
    }

    /**
     * 鉴权交易
     */
    private void onVerifyPay() {
        Intent intent = new Intent();
        intent.setComponent(new ComponentName("com.shengpay.smartpos.shengpaysdk","com.shengpay.smartpos.shengpaysdk.activity.VerifyActivity"));
        intent.putExtra("appId", getPackageName());
        intent.putExtra("amount", "000000000001");
//        intent.putExtra("signType", signType);
//        String pack = packUtil.compose(intent);
//        String signMsg = MD5.sign(pack + privateKey).toUpperCase();
        intent.putExtra("verifyServiceMode", mode);
//        intent.putExtra("signMsg", signMsg);
//        intent.putExtra("verifyInfo", pack);
        startActivityForResult(intent, 0);

    }

    /**
     * 硬件接口
     */
    private void initService() {
        Intent terminalIntent = new Intent();
        terminalIntent.setClassName("com.shengpay.smartpos.shengpaysdk","com.shengpay.smartpos.shengpaysdk.Service.TerminalService");
        TerminalServConn terminalServConn = new TerminalServConn();
        bindService(terminalIntent, terminalServConn, Context.BIND_AUTO_CREATE);
    }

    private class TerminalServConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
//            terminalAidl = ITerminalAidl.Stub.asInterface(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }



    class RFReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra("result");
//            tvCardInfo.setText(result);
        }
    }


    @Override
    protected void onActivityResult(int requestCode
            , int resultCode, Intent data) {
        switch(resultCode) {
            case Activity.RESULT_CANCELED:
                Log.d(TAG, "onActivityResult==>RESULT_CANCELED...");
                String reason = data.getStringExtra("reason");
                if(reason != null) {
                    Toast.makeText(ShengPayActivity.this, reason, Toast.LENGTH_SHORT).show();
                }
                break;
            case Activity.RESULT_OK:
                Log.d(TAG, "onActivityResult==>RESULT_OK...");
                break;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rfReceiver != null) {
            unregisterReceiver(rfReceiver);
        }
    }
}
