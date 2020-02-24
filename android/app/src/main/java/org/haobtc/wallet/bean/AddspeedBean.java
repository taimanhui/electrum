package org.haobtc.wallet.bean;

import com.google.gson.annotations.SerializedName;

public class AddspeedBean {

    /**
     * current_feerate : 180 sat/byte
     * new_feerate : 270.0
     */

    @SerializedName("current_feerate")
    private String currentFeerate;
    @SerializedName("new_feerate")
    private String newFeerate;

    public String getCurrentFeerate() {
        return currentFeerate;
    }

    public void setCurrentFeerate(String currentFeerate) {
        this.currentFeerate = currentFeerate;
    }

    public String getNewFeerate() {
        return newFeerate;
    }

    public void setNewFeerate(String newFeerate) {
        this.newFeerate = newFeerate;
    }
}
