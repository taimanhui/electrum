package org.haobtc.wallet.event;

public class ReceiveXpub {
    String xpub;

    public ReceiveXpub(String xpub) {
        this.xpub = xpub;
    }

    public String getXpub() {
        return xpub;
    }
}
