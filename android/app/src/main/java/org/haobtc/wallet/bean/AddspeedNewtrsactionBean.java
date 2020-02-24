package org.haobtc.wallet.bean;

import com.google.gson.annotations.SerializedName;

public class AddspeedNewtrsactionBean {

    /**
     * new_tx : 70736274ff01007102000000017035d4e58c3bf4de77564092beb87dd820fecceeb6a9c55b316fea696539e3040100000000fdffffff020a000000000000001600140ef8fb8667f68312e5b1f5f15fa7c5da3ee4776242b9f40500000000160014d781f4fc1600b3fdd55e4ba56360009f6ff7b401483400000001011f024ef50500000000160014e0793aac834d3405a9e8cef2bd4ee134a4190957220603caeceaf84cb9c498c52f187323b7c951b0a697d5cf3e2f4a342b2cb85c60f3c80c0b3a67cd0100000003000000002202034735ca370252a76729e7a0c0d9eaae48dfae6eab1b98668420acefad9c9bdb6f0c0b3a67cd00000000000000000000
     */

    @SerializedName("new_tx")
    private String newTx;

    public String getNewTx() {
        return newTx;
    }

    public void setNewTx(String newTx) {
        this.newTx = newTx;
    }
}
