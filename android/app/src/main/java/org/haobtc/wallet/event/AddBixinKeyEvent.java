package org.haobtc.wallet.event;

import java.io.Serializable;

public class AddBixinKeyEvent implements Serializable {
    private String keyname;
    private String keyaddress;
    private String device_id;

    public String getDevice_id() {
        return device_id;
    }

    public void setDevice_id(String device_id) {
        this.device_id = device_id;
    }

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
