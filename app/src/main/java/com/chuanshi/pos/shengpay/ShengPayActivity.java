package com.chuanshi.pos.shengpay;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.chuanshi.pos.R;


/**
 * Created by zhouliancheng on 2018/1/30.
 */

public class ShengPayActivity extends Activity {
    private static final String TAG = "ShengPayActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sheng_pay);
        Button mShengPayBtn = findViewById(R.id.btn_sheng_pay);
        mShengPayBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onShengPay("0", "000000000001");
            }
        });
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

}
