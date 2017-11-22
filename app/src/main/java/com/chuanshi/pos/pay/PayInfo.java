package com.chuanshi.pos.pay;

import com.chuanshi.pos.utils.IEntity;

/**
 * Created by zhoulc on 17/11/19.
 * 交易类
 */

public class PayInfo implements IEntity {
    private String msg_tp;//报文类型
    private String pay_tp;//支付方式
    private String proc_tp;//交易类型
    private String proc_cd;//交易处理码
    private String systraceno;//凭证号
    private String amt;//交易金额
    private String 订单号;//订单号（非必填）
    private String batchbillno;//批次流水号（非必填）
    private String appid;//应用包名
    private String time_stamp;//交易时间戳
    private String print_info;//打印信息（非必填）
    private String reason;//失败原因
    private String txndetail;//交易详情
    private String sysoldtraceno;//原交易凭证号
    private String oprId;//操作员

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

    public String getProc_tp() {
        return proc_tp;
    }

    public void setProc_tp(String proc_tp) {
        this.proc_tp = proc_tp;
    }

    public String getProc_cd() {
        return proc_cd;
    }

    public void setProc_cd(String proc_cd) {
        this.proc_cd = proc_cd;
    }

    public String getSystraceno() {
        return systraceno;
    }

    public void setSystraceno(String systraceno) {
        this.systraceno = systraceno;
    }

    public String getAmt() {
        return amt;
    }

    public void setAmt(String amt) {
        this.amt = amt;
    }

    public String get订单号() {
        return 订单号;
    }

    public void set订单号(String 订单号) {
        this.订单号 = 订单号;
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

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTime_stamp(String time_stamp) {
        this.time_stamp = time_stamp;
    }

    public String getPrint_info() {
        return print_info;
    }

    public void setPrint_info(String print_info) {
        this.print_info = print_info;
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

    public String getSysoldtraceno() {
        return sysoldtraceno;
    }

    public void setSysoldtraceno(String sysoldtraceno) {
        this.sysoldtraceno = sysoldtraceno;
    }

    public String getOprId() {
        return oprId;
    }

    public void setOprId(String oprId) {
        this.oprId = oprId;
    }
}
