package org.haobtc.wallet.event;

public class SetShutdownTimeEvent {
    String time;

    public SetShutdownTimeEvent(String time) {
        this.time = time;
    }

    public String getTime() {
        return time;
    }
}
