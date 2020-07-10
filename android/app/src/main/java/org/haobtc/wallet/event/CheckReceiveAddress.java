package org.haobtc.wallet.event;

public class CheckReceiveAddress {
    private String type;

    public CheckReceiveAddress(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }
}
