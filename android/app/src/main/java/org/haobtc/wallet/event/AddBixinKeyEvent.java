package org.haobtc.wallet.event;

import java.io.Serializable;

public class AddBixinKeyEvent implements Serializable {
    private String keyname;
    private String keyaddress;

    public String getKeyname() {
        return keyname;
    }

    public void setKeyname(String keyname) {
        this.keyname = keyname;
    }

    public String getKeyaddress() {
        return keyaddress;
    }

    public void setKeyaddress(String keyaddress) {
        this.keyaddress = keyaddress;
    }
}
