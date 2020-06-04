package org.haobtc.wallet.event;

public class PersonalMutiSigEvent {
    private String xpub;
    private String device_id;
    private String label;

    public PersonalMutiSigEvent(String xpub, String device_id, String label) {
        this.xpub = xpub;
        this.device_id = device_id;
        this.label = label;
    }

    public String getXpub() {
        return xpub;
    }

    public String getDevice_id() {
        return device_id;
    }

    public String getLabel() {
        return label;
    }
}
