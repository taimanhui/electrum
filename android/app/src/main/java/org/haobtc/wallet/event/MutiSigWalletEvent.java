package org.haobtc.wallet.event;

public class MutiSigWalletEvent {
    private String xpub;
    private String device_id;
    private String lable;

    public MutiSigWalletEvent(String xpub, String device_id, String lable) {
        this.xpub = xpub;
        this.device_id = device_id;
        this.lable = lable;
    }

    public String getXpub() {
        return xpub;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getLable() {
        return lable;
    }
}
