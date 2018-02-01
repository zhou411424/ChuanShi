package com.chuanshi.pos;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.chuanshi.pos.entity.GoodInfo;
import com.chuanshi.pos.entity.PayType;
import com.chuanshi.pos.library.http.NetworkUtil;
import com.chuanshi.pos.utils.Constants;
import com.chuanshi.pos.utils.SoundPlayer;
import com.chuanshi.pos.utils.WorkHandler;
import com.chuanshi.pos.webview.CustomWebChromeClient;
import com.chuanshi.pos.webview.CustomWebViewClient;
import com.chuanshi.pos.widget.CustomWebView;
import com.nld.cloudpos.aidl.AidlDeviceService;
import com.nld.cloudpos.aidl.printer.AidlPrinter;
import com.nld.cloudpos.aidl.printer.AidlPrinterListener;
import com.nld.cloudpos.aidl.printer.PrintItemObj;
import com.nld.cloudpos.data.PrinterConstant;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
    private SoundPlayer mSoundPlayer;

    private AidlDeviceService aidlDeviceService = null;
    private AidlPrinter aidlPrinter = null;
    private PrintPerformRunnable mPrintPerformRunnable;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "bind device service");
            aidlDeviceService = AidlDeviceService.Stub.asInterface(service);
            Log.d(TAG, "获取打印机设备实例...");
            try {
                aidlPrinter = AidlPrinter.Stub.asInterface(aidlDeviceService.getPrinter());
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "unbind device service");
            aidlDeviceService = null;
        }
    };

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

        //播放语音
        mSoundPlayer = new SoundPlayer(this);

        //绑定打印服务
        bindService(new Intent("nld_cloudpos_device_service"), serviceConnection, Context.BIND_AUTO_CREATE);

        Button mPrintBtn = findViewById(R.id.btn_print);
        Button mPrintBillBtn = findViewById(R.id.btn_print_bill);
        mPrintBtn.setOnClickListener(this);
        mPrintBillBtn.setOnClickListener(this);
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
//            String url = "http://www.csshidai.com";
            String url = "http://www.chuanshitech.com";
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

        /**
         * 播放语音提示
         */
        @JavascriptInterface
        public void playOrderNotice() {
            playSound();
        }

        /**
         * 开始打印预结单
         * @param preformJsonStr
         */
        @JavascriptInterface
        public void startPrintPreform(String preformJsonStr) {
            printPreform(preformJsonStr);
        }

        /**
         * 开始打印结账单
         * @param billJsonStr
         */
        @JavascriptInterface
        public void startPrintBill(String billJsonStr) {
            printBill(billJsonStr);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_reload:
                loadData();
                break;
            case R.id.btn_print:
                String json = "{\n" +
                        "    \"title\": \"杭州君悦大酒店(预结单)\",\n" +
                        "    \"table\": \"001\",\n" +
                        "    \"orderNumber\": \"2017113423232323\",\n" +
                        "    \"time\": \"2017-11-30 12:24:31\",\n" +
                        "    \"goodsList\": [\n" +
                        "        {\n" +
                        "            \"name\": \"牛肉水饺\",\n" +
                        "            \"num\": \"12.0*1.0\",\n" +
                        "            \"amount\": \"12\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"大葱大肉\",\n" +
                        "            \"num\": \"15.0*1.0\",\n" +
                        "            \"amount\": \"22\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"鸭子毛调蒜汁\",\n" +
                        "            \"num\": \"3.0*1.0\",\n" +
                        "            \"amount\": \"22\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"heji\": \"56\",\n" +
                        "    \"actualPayAmount\": \"12.0\",\n" +
                        "    \"couponAmount\": \"0.0\"\n" +
                        "}";
                printPreform(json);

                break;
            case R.id.btn_print_bill:
                String billJsonStr = "{\n" +
                        "    \"title\": \"杭州君悦大酒店(结账单)\",\n" +
                        "    \"table\": \"001\",\n" +
                        "    \"orderNumber\": \"2017113423232323\",\n" +
                        "    \"time\": \"2017-11-30 12:24:31\",\n" +
                        "    \"goodsList\": [\n" +
                        "        {\n" +
                        "            \"name\": \"牛肉水饺\",\n" +
                        "            \"num\": \"12.0*1.0\",\n" +
                        "            \"amount\": \"12\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"大葱大肉\",\n" +
                        "            \"num\": \"15.0*1.0\",\n" +
                        "            \"amount\": \"22\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"name\": \"鸭子毛调蒜汁\",\n" +
                        "            \"num\": \"3.0*1.0\",\n" +
                        "            \"amount\": \"22\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"payTypeList\": [\n" +
                        "        {\n" +
                        "            \"payType\": \"支付宝\",\n" +
                        "            \"amount\": \"22\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"payType\": \"微信\",\n" +
                        "            \"amount\": \"22\"\n" +
                        "        },\n" +
                        "        {\n" +
                        "            \"payType\": \"现金\",\n" +
                        "            \"amount\": \"22\"\n" +
                        "        }\n" +
                        "    ],\n" +
                        "    \"heji\": \"56\",\n" +
                        "    \"sdje\": \"100\",\n" +
                        "    \"zl\": \"44\",\n" +
                        "    \"actualPayAmount\": \"12.0\",\n" +
                        "    \"couponAmount\": \"0.0\",\n" +
                        "    \"memberNo\": \"0001\",\n" +
                        "    \"remainAmount\": \"2000\",\n" +
                        "    \"welcome\": \"谢谢惠顾,欢迎下次光临!\"\n" +
                        "}";
                printBill(billJsonStr);
                break;

        }
    }

    /**
     * 打印功能pintPreform预结单
     */
    private void printPreform(String json) {
        String title = "", table = "", orderNumber = "",
                time = "", heji="",actualPayAmount="", couponAmount="";
        List<GoodInfo> goodInfos = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(json)) {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject != null) {
                    title = jsonObject.getString("title");
                    table = jsonObject.getString("table");
                    orderNumber = jsonObject.getString("orderNumber");
                    time = jsonObject.getString("time");
                    heji = jsonObject.getString("heji");
                    actualPayAmount = jsonObject.getString("actualPayAmount");
                    couponAmount = jsonObject.getString("couponAmount");
                    JSONArray goodsListJsonArray = jsonObject.getJSONArray("goodsList");
                    if (goodsListJsonArray != null) {
                        for(int i=0;i<goodsListJsonArray.length();i++) {
                            JSONObject goodInfoJsonObject = goodsListJsonArray.getJSONObject(i);
                            if (goodInfoJsonObject != null) {
                                GoodInfo goodInfo = new GoodInfo();
                                goodInfo.setName(goodInfoJsonObject.getString("name"));
                                goodInfo.setNum(goodInfoJsonObject.getString("num"));
                                goodInfo.setAmount(goodInfoJsonObject.getString("amount"));
                                goodInfos.add(goodInfo);
                            }
                        }
                    }

                }
            }


            Log.d(TAG, "初始化打印机实例");

            if (null != aidlPrinter) {
                //文本内容
                final List<PrintItemObj> data = new ArrayList<PrintItemObj>();
                if (!TextUtils.isEmpty(title)) {
                    data.add(new PrintItemObj(title, PrinterConstant.FontScale.FONTSCALE_DW_DH,
                            PrinterConstant.FontType.FONTTYPE_S, PrintItemObj.ALIGN.LEFT, false, 6));
                }

                data.add(new PrintItemObj("\r"));

                if (!TextUtils.isEmpty(table)) {
                    data.add(new PrintItemObj("桌台："+table, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                }
                if (!TextUtils.isEmpty(orderNumber)) {
                    data.add(new PrintItemObj("单号："+orderNumber, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                }
                if (!TextUtils.isEmpty(time)) {
                    data.add(new PrintItemObj("时间："+time, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                }
                data.add(new PrintItemObj("********************************", PrinterConstant.FontScale.FONTSCALE_W_H,
                        PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));

                data.add(new PrintItemObj("名称           价*量       金额", PrinterConstant.FontScale.FONTSCALE_W_H,
                        PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                if (goodInfos != null && !goodInfos.isEmpty()) {
                    for (int i = 0;i < goodInfos.size(); i++) {
                        GoodInfo goodInfo = goodInfos.get(i);
                        if (goodInfo != null) {
                            Log.d(TAG, "name="+goodInfo.getName()+", num="+goodInfo.getNum()+", amount="+goodInfo.getAmount());
                            data.add(new PrintItemObj(autoNameString(goodInfo.getName()) + autoNumString(goodInfo.getNum()) + goodInfo.getAmount(),
                                    PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                        }
                    }
                }

                data.add(new PrintItemObj("********************************", PrinterConstant.FontScale.FONTSCALE_W_H,
                        PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));

                if (!TextUtils.isEmpty(heji)) {
                    data.add(new PrintItemObj("合计："+heji, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.RIGHT, false, 6));
                }

                data.add(new PrintItemObj("********************************", PrinterConstant.FontScale.FONTSCALE_W_H,
                        PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                if (!TextUtils.isEmpty(actualPayAmount) && !TextUtils.isEmpty(couponAmount)) {
                    data.add(new PrintItemObj("实付金额："+autoActualPayAmountString(actualPayAmount)+"优惠："+couponAmount, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                }

                data.add(new PrintItemObj("\r"));
                if (data != null) {
                    if (mPrintPerformRunnable == null) {
                        mPrintPerformRunnable = new PrintPerformRunnable(data);
                    } else {
                        mPrintPerformRunnable.setData(data);
                    }
                    WorkHandler.post2work(mPrintPerformRunnable);
                }
            } else {
                Log.d(TAG, "请检查打印数据data和打印机状况");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 打印结账单
     * @param json
     */
    private void printBill(String json) {
        String title = "", table = "", orderNumber = "",
                time = "", heji="",actualPayAmount="", couponAmount="",
                memberNo = "", remainAmount="", welcome = "",
                sdje = "", zl = "";
        List<GoodInfo> goodInfos = new ArrayList<>();
        List<PayType> payTypes = new ArrayList<>();
        try {
            if (!TextUtils.isEmpty(json)) {
                JSONObject jsonObject = new JSONObject(json);
                if (jsonObject != null) {
                    title = jsonObject.getString("title");
                    table = jsonObject.getString("table");
                    orderNumber = jsonObject.getString("orderNumber");
                    time = jsonObject.getString("time");
                    heji = jsonObject.getString("heji");
                    sdje = jsonObject.getString("sdje");//收到金额
                    zl = jsonObject.getString("zl");//找零
                    actualPayAmount = jsonObject.getString("actualPayAmount");
                    couponAmount = jsonObject.getString("couponAmount");
                    JSONArray goodsListJsonArray = jsonObject.getJSONArray("goodsList");
                    if (goodsListJsonArray != null) {
                        for(int i=0;i<goodsListJsonArray.length();i++) {
                            JSONObject goodInfoJsonObject = goodsListJsonArray.getJSONObject(i);
                            if (goodInfoJsonObject != null) {
                                GoodInfo goodInfo = new GoodInfo();
                                goodInfo.setName(goodInfoJsonObject.getString("name"));
                                goodInfo.setNum(goodInfoJsonObject.getString("num"));
                                goodInfo.setAmount(goodInfoJsonObject.getString("amount"));
                                goodInfos.add(goodInfo);
                            }
                        }
                    }
                    JSONArray payTypeListJsonArray = jsonObject.getJSONArray("payTypeList");
                    if (payTypeListJsonArray != null) {
                        for(int i=0;i<payTypeListJsonArray.length();i++) {
                            JSONObject payTypeJsonObject = payTypeListJsonArray.getJSONObject(i);
                            if (payTypeJsonObject != null) {
                                PayType payType = new PayType();
                                payType.setPayType(payTypeJsonObject.getString("payType"));
                                payType.setAmount(payTypeJsonObject.getString("amount"));
                                payTypes.add(payType);
                            }
                        }
                    }
                    memberNo = jsonObject.getString("memberNo");
                    remainAmount = jsonObject.getString("remainAmount");
                    welcome = jsonObject.getString("welcome");
                }
            }


            Log.d(TAG, "初始化打印机实例");

            if (null != aidlPrinter) {
                //文本内容
                final List<PrintItemObj> data = new ArrayList<PrintItemObj>();
                if (!TextUtils.isEmpty(title)) {
                    data.add(new PrintItemObj(title, PrinterConstant.FontScale.FONTSCALE_DW_DH,
                            PrinterConstant.FontType.FONTTYPE_S, PrintItemObj.ALIGN.LEFT, false, 6));
                }

                data.add(new PrintItemObj("\r"));

                if (!TextUtils.isEmpty(table)) {
                    data.add(new PrintItemObj("桌台："+table, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                }
                if (!TextUtils.isEmpty(orderNumber)) {
                    data.add(new PrintItemObj("单号："+orderNumber, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                }
                if (!TextUtils.isEmpty(time)) {
                    data.add(new PrintItemObj("时间："+time, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                }
                data.add(new PrintItemObj("********************************", PrinterConstant.FontScale.FONTSCALE_W_H,
                        PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));

                data.add(new PrintItemObj("名称           价*量       金额", PrinterConstant.FontScale.FONTSCALE_W_H,
                        PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                if (goodInfos != null && !goodInfos.isEmpty()) {
                    for (int i = 0;i < goodInfos.size(); i++) {
                        GoodInfo goodInfo = goodInfos.get(i);
                        if (goodInfo != null) {
                            Log.d(TAG, "name="+goodInfo.getName()+", num="+goodInfo.getNum()+", amount="+goodInfo.getAmount());
                            Log.d(TAG, "-----------name length="+goodInfo.getName().length()+", num length="+goodInfo.getNum().length());
                            data.add(new PrintItemObj(autoNameString(goodInfo.getName()) + autoNumString(goodInfo.getNum())+ goodInfo.getAmount(),
                                    PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                        }
                    }
                }

                data.add(new PrintItemObj("********************************", PrinterConstant.FontScale.FONTSCALE_W_H,
                        PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));

                if (!TextUtils.isEmpty(heji)) {
                    data.add(new PrintItemObj("合计："+heji, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.RIGHT, false, 6));
                }

                data.add(new PrintItemObj("********************************", PrinterConstant.FontScale.FONTSCALE_W_H,
                        PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));

                data.add(new PrintItemObj("支付方式                   金额", PrinterConstant.FontScale.FONTSCALE_W_H,
                        PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));

                if (payTypes != null && !payTypes.isEmpty()) {
                    for (int i = 0;i < payTypes.size(); i++) {
                        PayType payType = payTypes.get(i);
                        if (payType != null) {
                            data.add(new PrintItemObj(autoPayTypeString(payType.getPayType()) + payType.getAmount(),
                                    PrinterConstant.FontScale.FONTSCALE_W_H, PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                        }
                    }
                }

                data.add(new PrintItemObj("********************************", PrinterConstant.FontScale.FONTSCALE_W_H,
                        PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));

                if (!TextUtils.isEmpty(sdje) && !TextUtils.isEmpty(zl)) {
                    data.add(new PrintItemObj("收到金额："+autoActualPayAmountString(sdje)+"找零："+zl, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                }

                if (!TextUtils.isEmpty(actualPayAmount) && !TextUtils.isEmpty(couponAmount)) {
                    data.add(new PrintItemObj("实付金额："+autoActualPayAmountString(actualPayAmount)+"优惠："+couponAmount, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                }

                if (!TextUtils.isEmpty(memberNo)) {
                    data.add(new PrintItemObj("卡号："+memberNo, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                }
                if (!TextUtils.isEmpty(remainAmount)) {
                    data.add(new PrintItemObj("余额："+remainAmount, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.LEFT, false, 6));
                }
                if (!TextUtils.isEmpty(welcome)) {
                    data.add(new PrintItemObj(welcome, PrinterConstant.FontScale.FONTSCALE_W_H,
                            PrinterConstant.FontType.FONTTYPE_N, PrintItemObj.ALIGN.CENTER, false, 6));
                }

                data.add(new PrintItemObj("\r"));
                if (data != null) {
                    if (mPrintPerformRunnable == null) {
                        mPrintPerformRunnable = new PrintPerformRunnable(data);
                    } else {
                        mPrintPerformRunnable.setData(data);
                    }
                    WorkHandler.post2work(mPrintPerformRunnable);
                }
            } else {
                Log.d(TAG, "请检查打印数据data和打印机状况");
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 自动补齐名称空格
     * @param name
     */
    private String autoNameString(String name) {
        if (TextUtils.isEmpty(name)) {
            return "            ";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (name.length() == 1) {
            sb.append("            ");//12个空格
        } else if (name.length() == 2) {
            sb.append("          ");//10个空格
        } else if(name.length() == 3) {
            sb.append("        ");//8个空格
        } else if (name.length() == 4) {
            sb.append("      ");//6个空格
        } else if (name.length() == 5) {
            sb.append("    ");//4个空格
        } else if (name.length() == 6) {
            sb.append("  ");//2个空格
        }
        return sb.toString();
    }

    /**
     * 自动补齐价*量空格
     * @param num
     * @return
     */
    private String autoNumString(String num) {
        if (TextUtils.isEmpty(num)) {
            return "            ";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(num);
        if (num.length() == 4) {
            sb.append("          ");//10个空格
        } else if (num.length() == 5) {
            sb.append("         ");//9个空格
        } else if(num.length() == 6) {
            sb.append("        ");//8个空格
        } else if (num.length() == 7) {
            sb.append("       ");//6个空格
        } else if (num.length() == 8) {
            sb.append("      ");//6个空格
        } else if (num.length() == 9) {
            sb.append("     ");//5个空格
        }
        return sb.toString();
    }

    /**
     * 自动补齐支付方式空格
     * @param payType
     * @return
     */
    private String autoPayTypeString(String payType) {
        if (TextUtils.isEmpty(payType)) {
            return "            ";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(payType);
        if (payType.length() == 1) {
            sb.append("                          ");//26个空格
        } else if (payType.length() == 2) {
            sb.append("                        ");//24个空格
        } else if(payType.length() == 3) {
            sb.append("                      ");//22个空格
        } else if (payType.length() == 4) {
            sb.append("                    ");//20个空格
        } else if (payType.length() == 5) {
            sb.append("                  ");//18个空格
        } else if (payType.length() == 6) {
            sb.append("                ");//16个空格
        }
        return sb.toString();
    }

    /**
     * 自动补齐实付金额或收到金额空格
     * @param str
     * @return
     */
    private String autoActualPayAmountString(String str) {
        if (TextUtils.isEmpty(str)) {
            return "            ";
        }
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        if (str.length() == 1) {
            sb.append("            ");//12个空格
        } else if (str.length() == 2) {
            sb.append("           ");//11个空格
        } else if(str.length() == 3) {
            sb.append("          ");//10个空格
        } else if (str.length() == 4) {
            sb.append("         ");//9个空格
        } else if (str.length() == 5) {
            sb.append("        ");//8个空格
        } else if (str.length() == 6) {
            sb.append("       ");//7个空格
        }
        return sb.toString();
    }

    /**
     * 打印预结单
     */
    private class PrintPerformRunnable implements Runnable {
        private List<PrintItemObj> data;
        public PrintPerformRunnable(List<PrintItemObj> data) {
            this.data = data;
        }

        public void setData(List<PrintItemObj> data) {
            this.data = data;
        }

        @Override
        public void run() {
            if (aidlPrinter != null && data != null) {
                try {
                    aidlPrinter.open();
                    //打印文本
                    aidlPrinter.printText(data);

                    aidlPrinter.start(new AidlPrinterListener.Stub() {

                        @Override
                        public void onPrintFinish() throws RemoteException {
                            Log.e(TAG, "打印结束");
                            //**如果出现纸撕下部分有未输出的内容释放下面代码**//*
                            aidlPrinter.paperSkip(2);
                        }

                        @Override
                        public void onError(int errorCode) throws RemoteException {
                            Log.d(TAG, "打印异常码:" + errorCode);
                        }
                    });

                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 播放语音提醒
     */
    private void playSound() {
        WorkHandler.post2work(mPlayGiftSoundRunnable);
    }

    /*private void stopSound() {
        WorkHandler.post2work(mStopGiftSoundRunnable);
    }

    private Runnable mStopGiftSoundRunnable = new Runnable() {
        @Override
        public void run() {
            mSoundPlayer.stopSound();
        }
    };*/

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
//        WorkHandler.removeRunnale(mStopGiftSoundRunnable);
        if (mPrintPerformRunnable != null) {
            WorkHandler.removeRunnale(mPrintPerformRunnable);
        }
        //解绑服务，清除打印机资源
        unbindService(serviceConnection);
        aidlPrinter=null;
        super.onDestroy();
    }
    
}
