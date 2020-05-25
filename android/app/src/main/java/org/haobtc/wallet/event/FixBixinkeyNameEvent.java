package org.haobtc.wallet.event;

public class FixBixinkeyNameEvent {
    String keyname;

    public FixBixinkeyNameEvent(String keyname) {
        this.keyname = keyname;
    }

    public String getKeyname() {
        return keyname;
    }
}
