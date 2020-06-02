package org.haobtc.wallet.event;

public class MutiSigWalletEvent {
    private String xpub;
    private String device_id;

    public MutiSigWalletEvent(String xpub, String device_id) {
        this.xpub = xpub;
        this.device_id = device_id;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getXpub() {
        return xpub;
    }

}
