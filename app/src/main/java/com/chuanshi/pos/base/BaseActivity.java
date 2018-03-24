package com.chuanshi.pos.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.shengpay.smartpos.shengpaysdk.ITerminalAidl;

import java.util.ArrayList;

/**
 * Created by lizheren on 2016/12/29.
 */
public class BaseActivity extends AppCompatActivity {
    private static final String TAG = "BaseActivity";
    public ITerminalAidl terminalAidl;
    private TerminalInfoServConn terminalInfoServConn;
    public boolean b;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent terminalIntent = new Intent();
        terminalIntent.setClassName("com.shengpay.smartpos.shengpaysdk", "com.shengpay.smartpos.shengpaysdk.Service.TerminalService");
        terminalInfoServConn = new TerminalInfoServConn();
        b = bindService(terminalIntent, terminalInfoServConn, Context.BIND_AUTO_CREATE);
    }

    private class TerminalInfoServConn implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

            terminalAidl = ITerminalAidl.Stub.asInterface(service);
            Log.d(TAG,"连接服务");
            Log.d(TAG, "terminalAidl="+(terminalAidl == null));
            serviceConnected();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"失去连接");
            Log.d(TAG,terminalAidl+"");
        }
    }

    /**s
     * 服务连接成功时做操作
     */
    protected void serviceConnected() {
        Toast.makeText(this, "绑定打印服务成功", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (terminalInfoServConn != null) {
            unbindService(terminalInfoServConn);
            terminalInfoServConn = null;
        }
    }

    public ArrayList<String> splitString(String s) {
        ArrayList<String> list = new ArrayList<>();
        String[] split = s.split("\n");
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < split.length; i++) {
            if (sb.length() + i < 106) sb.append(split[i] + "\n");
            if (sb.length() + i >= 106) {
                String substring = sb.toString().substring(0, 105 - i);
                list.add(substring);
                sb.delete(0, 105);
            }
        }
        return list;
    }
}
