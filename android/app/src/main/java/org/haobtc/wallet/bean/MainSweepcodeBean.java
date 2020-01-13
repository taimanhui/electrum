package org.haobtc.wallet.bean;

import com.google.gson.annotations.SerializedName;

public class MainSweepcodeBean {

    /**
     * address : bcrt1qnuh3qc9g6lwlqqvmf7hg05pzlujhua9emdqdty4znjstr5886paqhwp25r
     */

    @SerializedName("address")
    private String address;

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
