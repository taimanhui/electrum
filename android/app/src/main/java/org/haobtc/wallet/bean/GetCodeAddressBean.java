package org.haobtc.wallet.bean;

public class GetCodeAddressBean {

    /**
     * qr_data : bitcoin:tb1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paq6htvpe
     * addr : tb1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paq6htvpe
     */

    private String qr_data;
    private String addr;

    public String getQr_data() {
        return qr_data;
    }

    public void setQr_data(String qr_data) {
        this.qr_data = qr_data;
    }

    public String getAddr() {
        return addr;
    }

    public void setAddr(String addr) {
        this.addr = addr;
    }
}
