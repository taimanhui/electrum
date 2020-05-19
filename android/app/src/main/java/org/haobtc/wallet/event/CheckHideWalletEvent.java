package org.haobtc.wallet.event;

public class CheckHideWalletEvent {
    private String xpub;

    public CheckHideWalletEvent(String xpub) {
        this.xpub = xpub;
    }

    public String getXpub() {
        return xpub;
    }
}
