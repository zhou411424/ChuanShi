package com.chuanshi.pos.utils;

import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.Calendar;

/**
 * Created by zhoulc on 18/2/4.
 */

public class NumberUtils {
    /**
     * "10.55"转成"000000001055"格式
     * @param amount
     */
    public static String doubleToShengPayAmount(String amount) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(amount)) {
            double dAmount = Double.parseDouble(amount);
            int iAmount = (int) (dAmount * 100);
            amount = iAmount +"";
            if (amount.length() < 12) {//amount最长12位
                for (int i =0 ;i < 12-amount.length();i++) {
                    sb.append("0");
                }
                sb.append(amount);

            }
        }
        return sb.toString();
    }

    /**
     * "000000001055"转成"10.55"格式
     * @param amount
     */
    public static String shengPayAmountToDouble(String amount) {
        if (!TextUtils.isEmpty(amount)) {
            char[] arr = amount.toCharArray();
            int count = 0;//计算字符串中前面0的个数
            for (int i = 0; i < arr.length; i++) {
                if(i == '0') {
                    count++;
                } else {
                    break;
                }
            }
            if (amount.length() >= count && count > 0) {
                amount = amount.substring(count - 1,amount.length());
            }

            double dAmount = Double.parseDouble(amount);
            BigDecimal bg = new BigDecimal(dAmount);
            double newAmount = bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
            amount = newAmount / 100 +"";
        }
        return amount;
    }

    public static int getYear() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        return year;
    }
}
