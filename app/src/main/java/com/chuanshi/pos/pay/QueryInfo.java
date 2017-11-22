package com.chuanshi.pos.pay;

import com.chuanshi.pos.utils.IEntity;

/**
 * Created by zhoulc on 17/11/19.
 * 查询类
 */

public class QueryInfo implements IEntity {
    private String msg_tp;//报文类型
    private String pay_tp;//支付方式
    private String order_no;//订单号
    private String batchbillno;//批次流水号
    private String appid;//应用包名
    private String reason;//失败原因
    private String txndetail;//交易详情

    public String getMsg_tp() {
        return msg_tp;
    }

    public void setMsg_tp(String msg_tp) {
        this.msg_tp = msg_tp;
    }

    public String getPay_tp() {
        return pay_tp;
    }

    public void setPay_tp(String pay_tp) {
        this.pay_tp = pay_tp;
    }

    public String getOrder_no() {
        return order_no;
    }

    public void setOrder_no(String order_no) {
        this.order_no = order_no;
    }

    public String getBatchbillno() {
        return batchbillno;
    }

    public void setBatchbillno(String batchbillno) {
        this.batchbillno = batchbillno;
    }

    public String getAppid() {
        return appid;
    }

    public void setAppid(String appid) {
        this.appid = appid;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getTxndetail() {
        return txndetail;
    }

    public void setTxndetail(String txndetail) {
        this.txndetail = txndetail;
    }
}
