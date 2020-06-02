package org.haobtc.wallet.event;

public class ReceiveXpub {
    String xpub;
    String device_id;

    public ReceiveXpub(String xpub, String device_id) {
        this.xpub = xpub;
        this.device_id = device_id;
    }

    public String getXpub() {
        return xpub;
    }

    public String getDevice_id() {
        return device_id;
    }
}
