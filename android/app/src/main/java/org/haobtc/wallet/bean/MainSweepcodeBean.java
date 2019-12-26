package org.haobtc.wallet.bean;

import com.google.gson.annotations.SerializedName;

public class MainSweepcodeBean {

    /**
     * status : 1
     * data : mhZ5dTc91TxttEvFJifBNPNqwLAD5CxhYF
     */

    @SerializedName("status")
    private int status;
    @SerializedName("data")
    private String data;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
