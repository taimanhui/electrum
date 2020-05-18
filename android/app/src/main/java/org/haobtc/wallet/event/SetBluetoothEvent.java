package org.haobtc.wallet.event;

public class SetBluetoothEvent {
    String status;

    public SetBluetoothEvent(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
