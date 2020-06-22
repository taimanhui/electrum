package org.haobtc.wallet.event;

public class ReceiveXpub {
    String xpub;
    String device_id;
    Boolean needBackup;
    boolean showUI;

    public ReceiveXpub(String xpub, String device_id, Boolean needBackup, boolean show) {
        this.xpub = xpub;
        this.device_id = device_id;
        this.needBackup = needBackup;
        this.showUI = show;
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

    public void setShowUI(boolean showUI) {
        this.showUI = showUI;
    }

    public boolean isShowUI() {
        return showUI;
    }
}
