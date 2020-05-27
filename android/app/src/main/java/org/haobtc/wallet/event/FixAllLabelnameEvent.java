package org.haobtc.wallet.event;

public class FixAllLabelnameEvent {
    String keyname;
    String code;

    public FixAllLabelnameEvent(String keyname, String code) {
        this.keyname = keyname;
        this.code = code;

    }

    public String getKeyname() {
        return keyname;
    }

    public String getCode() {
        return code;
    }
}
