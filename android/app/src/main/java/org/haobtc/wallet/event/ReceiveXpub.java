package org.haobtc.wallet.event;

public class ReceiveXpub {
    String xpub;
    String device_id;
    Boolean needBackup;

    public ReceiveXpub(String xpub, String device_id, Boolean needBackup) {
        this.xpub = xpub;
        this.device_id = device_id;
        this.needBackup = needBackup;
    }

    public String getXpub() {
        return xpub;
    }

    public String getDevice_id() {
        return device_id;
    }

    public Boolean getNeedBackup() {
        return needBackup;
    }
}
